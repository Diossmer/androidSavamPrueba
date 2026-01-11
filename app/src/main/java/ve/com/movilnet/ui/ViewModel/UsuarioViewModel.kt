package ve.com.movilnet.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ve.com.movilnet.data.Model.UsuarioRequest
import ve.com.movilnet.utils.RetrofitClient
import ve.com.savam.data.models.Roles
import ve.com.savam.data.models.Usuario

class UsuarioViewModel : ViewModel() {

    // LiveData privado y mutable, solo el ViewModel puede cambiar su valor.
    private val _usuarios = MutableLiveData<List<Usuario>>()
    // LiveData público e inmutable, la vista solo puede observar sus cambios.
    val usuarios: LiveData<List<Usuario>> = _usuarios

    // LiveData para manejar errores o estados de carga.
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _roles = MutableLiveData<List<Roles>>()
    val roles: LiveData<List<Roles>> = _roles
    // --- NUEVO LIVE DATA PARA EL USUARIO SELECCIONADO ---
    private val _usuarioSeleccionado = MutableLiveData<Usuario?>()
    val usuarioSeleccionado: LiveData<Usuario?> = _usuarioSeleccionado
    // --- LiveData para el usuario que ha iniciado sesión ---
    private val _loggedInUser = MutableLiveData<Usuario?>()
    val loggedInUser: LiveData<Usuario?> = _loggedInUser


    /**
     * Establece el usuario que ha iniciado sesión.
     * Deberías llamar a esta función después de un login exitoso.
     */
    fun setLoggedInUser(usuario: Usuario?) {
        _loggedInUser.value = usuario
    }

    /**
     * Comprueba si el usuario logueado tiene el rol de "Administrador".
     * El nombre 'Administrador' debe coincidir exactamente con el que viene de la API.
     */
    private fun esUsuarioAdministrador(): Boolean {
        // Obtiene el usuario logueado del LiveData.
        val usuarioActual = _loggedInUser.value
        if (usuarioActual == null) {
            Log.d("AuthCheck", "No hay usuario logueado para verificar el rol.")
            return false // Si no hay nadie logueado, no es admin.
        }

        // --- CORRECCIÓN ---
        // 'usuarioActual.roles' es un objeto Roles?, no una lista.
        // Accedemos a su propiedad 'nombre' de forma segura con '?.'
        val esAdmin = usuarioActual.roles?.nombre?.equals("Administrador", ignoreCase = true) == true

        Log.d("AuthCheck", "Usuario: ${usuarioActual.nombre}, ¿Es Admin? $esAdmin")
        return esAdmin
    }


    // Llama a esta función para cargar la lista de usuarios.
    fun fetchUsuarios() {
        // viewModelScope se encarga de cancelar la corrutina si el ViewModel se destruye.
        viewModelScope.launch {
            _isLoading.value = true

            // --- ¡AQUÍ ESTÁ LA LÓGICA DE VALIDACIÓN! ---
            if (esUsuarioAdministrador()) {
                // Si es Administrador, busca todos los usuarios desde la API.
                Log.d("fetchUsuarios", "El usuario es Administrador. Obteniendo lista completa.")
                try {
                    // Llama al método de la API definido en UsuariosServices.
                    val response = RetrofitClient.usuariosServices.listUsuarios()
                    if (response.isSuccessful) {
                        // Si la respuesta es exitosa, actualiza el LiveData.
                        _usuarios.value = response.body()
                    } else {
                        // Si hay un error en la respuesta, notifícalo.
                        _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    // Si ocurre una excepción (ej: sin conexión), notifícalo.
                    _errorMessage.value = "Fallo en la conexión: ${e.message}"
                }
            } else {
                // Si NO es Administrador, muestra solo su propio perfil en la lista.
                Log.d("fetchUsuarios", "El usuario NO es Administrador. Mostrando solo su perfil.")
                val usuarioActual = _loggedInUser.value
                if (usuarioActual != null) {
                    // Crea una lista que contiene únicamente al usuario logueado.
                    _usuarios.value = listOf(usuarioActual)
                } else {
                    // Si por alguna razón no hay un usuario logueado, la lista estará vacía.
                    _usuarios.value = emptyList()
                    _errorMessage.value = "No se pudo obtener la información del usuario."
                    Log.d("fetchUsuarios", "Error: Se intentó obtener usuarios sin estar logueado.")
                }
            }

            _isLoading.value = false
        }
    }

    // ... (El resto de tus funciones: fetchRoles, agregarUsuario, etc., se mantienen igual)
    fun fetchRoles() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.rolesServices.listRoles()
                if (response.isSuccessful) {
                    _roles.value = response.body()
                } else {
                    _errorMessage.value = "Error al obtener roles: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo de conexión al obtener roles: ${e.message}"
            }
        }
    }

    // Dentro de la clase UsuarioViewModel, añade estas funciones:
    fun agregarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                // LA SOLUCIÓN: Obtener el primer elemento de la lista y LUEGO su id.
                // Igual que en `actualizarUsuario`.
                val rolId = usuario.roles?.id

                // Esta verificación ahora funcionará correctamente.
                if (rolId == null) {
                    _errorMessage.value = "Error: El rol seleccionado no es válido."
                    return@launch
                }

                // Creamos el objeto UsuarioRequest
                val requestRequest = UsuarioRequest(
                    id = null,
                    nombre = usuario.nombre,
                    apellido = usuario.apellido,
                    cedula = usuario.cedula,
                    correo = usuario.correo,
                    password = usuario.password,
                    oficina = usuario.oficina,
                    estado = usuario.estado,
                    // Usamos el `rolId` que obtuvimos de la lista.
                    roles = listOf(rolId)
                )

                // ¡OJO! La API al CREAR devuelve un 'Usuario' con "roles" como String.
                // Esto fallará la deserialización con Gson.
                // La solución ideal es pedir al backend que la respuesta al crear sea consistente
                // y devuelva el objeto rol completo.
                val response = RetrofitClient.usuariosServices.storeUsuario(requestRequest)

                if (response.isSuccessful) {
                    fetchUsuarios() // Refresca la lista después de agregar
                } else {
                    _errorMessage.value = "Error al agregar: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al agregar: ${e.message}"
            }
        }
    }

    fun mostrarUsuario(usuarioId: String) {
        // Debes usar una corrutina para llamadas suspend
        viewModelScope.launch {
            _isLoading.value = true // Indica que estás cargando algo
            try {
                val response = RetrofitClient.usuariosServices.showUsuario(usuarioId)
                if (response.isSuccessful) {
                    // SOLUCIÓN: Asigna el usuario único al nuevo LiveData
                    _usuarioSeleccionado.value = response.body()
                } else {
                    _errorMessage.value =
                        "Error al mostrar: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al mostrar: ${e.message}"
            }
            _isLoading.value = false // Termina la carga
        }
    }

    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                // Obtén el primer rol de la lista. Si no hay, no se podrá actualizar.
                val rolId = usuario.roles?.id
                if (usuario.id == null || rolId == null) {
                    _errorMessage.value = "Error: Faltan datos para actualizar (ID de usuario o rol)."
                    return@launch
                }

                // ----> TRANSFORMACIÓN CLAVE <----
                val usuarioRequest = UsuarioRequest(
                    id = usuario.id,
                    nombre = usuario.nombre,
                    apellido = usuario.apellido,
                    cedula = usuario.cedula,
                    correo = usuario.correo,
                    password = usuario.password, // El backend debe ignorar esto si es nulo
                    oficina = usuario.oficina,
                    estado = usuario.estado,
                    roles = listOf(rolId) // Solo el ID en una lista
                )

                val response = RetrofitClient.usuariosServices.updateUsuario(usuario.id, usuarioRequest)

                if (response.isSuccessful) {
                    fetchUsuarios() // Refresca la lista después de actualizar
                } else {
                    _errorMessage.value = "Error al actualizar: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al actualizar: ${e.message}"
            }
        }
    }

    fun eliminarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.usuariosServices.deletePost(usuarioId)
                if (response.isSuccessful) {
                    fetchUsuarios() // Refresca la lista después de eliminar
                } else {
                    _errorMessage.value = "Error al eliminar: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al eliminar: ${e.message}"
            }
        }
    }
}

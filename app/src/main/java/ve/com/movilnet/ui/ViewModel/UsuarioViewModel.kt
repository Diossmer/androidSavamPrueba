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
     * Limpia el LiveData del usuario seleccionado.
     * Es crucial llamar a esta función antes de abrir el formulario en modo "Agregar"
     * o al cerrar el formulario para evitar que datos antiguos se muestren.
     */
    fun limpiarUsuarioSeleccionado() {
        _usuarioSeleccionado.value = null
    }
    /**
     * Establece el usuario que ha iniciado sesión.
     * Deberías llamar a esta función después de un login exitoso.
     */
    fun setLoggedInUser(usuario: Usuario?) {
        _loggedInUser.value = usuario
    }

    /**
     * Comprueba si un usuario dado tiene el rol de "Administrador".
     * Esta función es privada para asegurar que la lógica de validación de roles
     * se mantenga encapsulada dentro del ViewModel.
     *
     * @param usuario El objeto Usuario a verificar.
     * @return `true` si el usuario no es nulo y su rol es "Administrador", `false` en caso contrario.
     */
    private fun esUsuarioAdministrador(usuario: Usuario?): Boolean {
        // Si el usuario que nos pasan es nulo, no puede ser admin.
        if (usuario == null) {
            Log.d("AuthCheck", "Chequeo de rol fallido: el objeto usuario es nulo.")
            return false
        }

        // Comparamos el nombre del rol de forma segura, ignorando mayúsculas/minúsculas.
        val esAdmin = usuario.roles?.nombre?.equals("Administrador", ignoreCase = true) == true

        Log.d(
            "AuthCheck",
            "Chequeando rol para: ${usuario.nombre}, Rol: '${usuario.roles?.nombre}', ¿Es Admin? -> $esAdmin"
        )
        return esAdmin
    }

    /**
     * Obtiene la lista de usuarios según el rol del usuario que ha iniciado sesión.
     * - Si es Administrador, obtiene la lista completa desde la API.
     * - Si no es Administrador, solo muestra el perfil del propio usuario.
     */
    fun fetchUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true

            val usuarioActual = _loggedInUser.value

            // --- VALIDACIÓN DE ROL DE ADMINISTRADOR ---
            // Se pasa el usuario actual a la función de validación.
            if (esUsuarioAdministrador(usuarioActual)) {
                // CASO 1: Es Administrador -> Visualiza la lista completa.
                Log.d("fetchUsuarios", "Usuario es Administrador. Obteniendo lista completa de la API.")
                try {
                    val response = RetrofitClient.usuariosServices.listUsuarios()
                    if (response.isSuccessful) {
                        // La vista que observa 'usuarios' se actualizará para mostrar a todos.
                        _usuarios.value = response.body()
                    } else {
                        _errorMessage.value = "Error al obtener usuarios: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    // Captura excepciones como falta de conexión a internet.
                    _errorMessage.value = "Fallo en la conexión: ${e.message}"
                }
            } else {
                // CASO 2: NO es Administrador (ej: Moderador, Agente, etc.)
                Log.d("fetchUsuarios", "Usuario NO es Administrador. Obteniendo lista y filtrando admins.")
                try {
                    // 1. Llama a la API para obtener la lista completa de usuarios.
                    val response = RetrofitClient.usuariosServices.listUsuarios()
                    if (response.isSuccessful) {
                        // 2. Filtra la lista para excluir a los administradores.
                        //    Usamos la misma función `esUsuarioAdministrador` que ya tienes.
                        val listaFiltrada = response.body()?.filter { usuario ->
                            !esUsuarioAdministrador(usuario)
                        }

                        // 3. Asigna la lista ya filtrada al LiveData.
                        _usuarios.value = listaFiltrada?: emptyList()
                    } else {
                        // Manejo de error si la llamada a la API falla.
                        _errorMessage.value = "Error al obtener usuarios: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    // Manejo de error si hay un problema de conexión.
                    _errorMessage.value = "Fallo en la conexión: ${e.message}"
                }
                /*
                // CASO 2: NO es Administrador (o no hay nadie logueado) -> No visualiza la lista.
                Log.d("fetchUsuarios", "Usuario NO es Administrador. Mostrando solo su propio perfil.")
                if (usuarioActual != null) {
                    // La vista solo mostrará al usuario logueado en la lista.
                    _usuarios.value = listOf(usuarioActual)
                } else {
                    // Si por alguna razón no hay un usuario, la lista estará vacía.
                    _usuarios.value = emptyList()
                    _errorMessage.value = "No se pudo obtener la información del usuario."
                    Log.w("fetchUsuarios", "Se intentó obtener usuarios sin haber iniciado sesión.")
                }
                */
            }

            // Indica que la operación de carga (exitosa o no) ha finalizado.
            _isLoading.value = false
        }
    }

    fun fetchRoles() {
        viewModelScope.launch {
            _isLoading.value = true // Opcional: puedes manejar un estado de carga para los roles

            try {
                // 1. Obtener la lista COMPLETA de roles desde la API
                val response = RetrofitClient.rolesServices.listRoles()

                if (response.isSuccessful) {
                    val todosLosRoles = response.body()

                    // 2. Obtener el usuario que ha iniciado sesión
                    val usuarioActual = _loggedInUser.value

                    // 3. Comprobar si el usuario actual NO es administrador
                    if (!esUsuarioAdministrador(usuarioActual)) {
                        // Si NO es Admin (ej: Moderador, Agente), filtramos la lista
                        Log.d("fetchRoles", "Usuario no es Administrador. Filtrando la lista de roles.")
                        val rolesFiltrados = todosLosRoles?.filter { rol ->
                            // Comparamos el nombre del rol, ignorando mayúsculas/minúsculas
                            !rol.nombre.equals("Administrador", ignoreCase = true)
                        }
                        // Asignamos la lista filtrada (sin el rol "Administrador")
                        _roles.value = rolesFiltrados ?: emptyList()
                    } else {
                        // Si ES Admin (o no hay usuario logueado, lo cual es un caso borde),
                        // mostramos la lista completa de roles.
                        Log.d("fetchRoles", "Usuario es Administrador. Mostrando todos los roles.")
                        _roles.value = todosLosRoles?: emptyList()
                    }
                } else {
                    // Manejo de error si la llamada a la API falla
                    _errorMessage.value = "Error al obtener roles: ${response.code()}"
                }
            } catch (e: Exception) {
                // Manejo de error de conexión
                _errorMessage.value = "Fallo de conexión al obtener roles: ${e.message}"
            } finally {
                _isLoading.value = false // Finaliza la carga
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

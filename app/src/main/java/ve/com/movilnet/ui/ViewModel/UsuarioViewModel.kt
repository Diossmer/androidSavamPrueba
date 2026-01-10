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
    // --- AÑADE ESTO: LiveData para el usuario que ha iniciado sesión ---
    private val _loggedInUser = MutableLiveData<Usuario?>()
    val loggedInUser: LiveData<Usuario?> = _loggedInUser

    // Esta función es la que establece quién es el usuario actual.
    // Deberías llamarla después de un inicio de sesión exitoso.
    fun setLoggedInUser(usuario: Usuario) {
        _loggedInUser.value = usuario
    }
    // Llama a esta función para simular la carga del usuario actual
    // En una app real, esta función obtendría el usuario guardado en SharedPreferences o una base de datos.
    fun loadCurrentUser() {
        viewModelScope.launch {
            // ESTA ES LA LÓGICA QUE DEBES ADAPTAR
            // Por ahora, para que funcione, vamos a simular que el usuario actual
            // es el primer administrador que encontremos en la lista.
            // Si no hay lista, no hacemos nada.
            val userList = _usuarios.value
            if (userList.isNullOrEmpty()) {
                // Si la lista de usuarios aún no se ha cargado, podríamos esperar o no hacer nada.
                // Por ahora, no hacemos nada.
                return@launch
            }

            // Buscamos un usuario con rol de "Administrador" para simular.
            // Cambia "Administrador" por el nombre del rol que uses.
            val adminUser = userList.find { it.roles?.nombre == "Administrador" }

            _loggedInUser.value = adminUser
        }
    }

    // Llama a esta función para cargar la lista de usuarios.
    fun fetchUsuarios() {
        // viewModelScope se encarga de cancelar la corrutina si el ViewModel se destruye.
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Llama al método de la API definido en UsuariosServices.
                val response = RetrofitClient.usuariosServices.listUsuarios()
                if (response.isSuccessful) {
                    // Si la respuesta es exitosa, actualiza el LiveData.
                    _usuarios.value = response.body()
                    // --- IMPORTANTE ---
                    // Una vez que tenemos la lista de usuarios, cargamos al usuario actual.
                    loadCurrentUser()
                } else {
                    // Si hay un error en la respuesta, notifícalo.
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                // Si ocurre una excepción (ej: sin conexión), notifícalo.
                _errorMessage.value = "Fallo en la conexión: ${e.message}"
            }
            _isLoading.value = false
        }
    }

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
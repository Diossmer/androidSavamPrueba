package ve.com.movilnet.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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
                val response = RetrofitClient.usuariosServices.storeUsuario(usuario)
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

    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            if (usuario.id == null) {
                _errorMessage.value = "Error: ID de usuario no puede ser nulo para actualizar."
                return@launch
            }
            try {
                val response = RetrofitClient.usuariosServices.patchPost(usuario.id, usuario)
                if (response.isSuccessful) {
                    fetchUsuarios() // Refresca la lista después de actualizar
                } else {
                    _errorMessage.value = "Error al actualizar: ${response.code()}"
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
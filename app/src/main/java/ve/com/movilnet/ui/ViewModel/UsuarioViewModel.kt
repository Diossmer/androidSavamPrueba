package ve.com.movilnet.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ve.com.movilnet.utils.RetrofitClient
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


    // Llama a esta función para cargar la lista de usuarios.
    fun fetchUsuarios() {
        // viewModelScope se encarga de cancelar la corrutina si el ViewModel se destruye.
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Llama al método de la API definido en UsuariosServices.
                val response = RetrofitClient.instance.listUsuarios()
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
}
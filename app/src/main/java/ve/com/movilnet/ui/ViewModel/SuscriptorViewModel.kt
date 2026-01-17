package ve.com.movilnet.ui.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ve.com.movilnet.data.Request.SuscriptorRequest
import ve.com.movilnet.data.Response.SuscriptorResponse
import ve.com.movilnet.data.Services.SuscriptorServices
import ve.com.savam.data.models.Suscriptor

class SuscriptorViewModel(private val suscriptorService: SuscriptorServices) :
    ViewModel() {
    // LiveData para la lista de suscriptores
    private val _suscriptores = MutableLiveData<List<Suscriptor>>()
    val suscriptores: LiveData<List<Suscriptor>> get() = _suscriptores

    // LiveData para manejar mensajes de éxito o error
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> get() = _mensaje

    // LiveData para controlar el estado de carga (ej. mostrar un ProgressBar)
    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    // --- MÉTODOS CRUD ---

    fun obtenerSuscriptores() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val response = suscriptorService.obtenerSuscriptores()
                if (response.isSuccessful) {
                    _suscriptores.value = response.body()
                } else {
                    _mensaje.value = "Error al obtener la lista: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _mensaje.value = "Excepción: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // Llama a este método desde tu Fragment para guardar el suscriptor
    fun guardarSuscriptor(suscriptor: SuscriptorRequest) {
        viewModelScope.launch {
            try {
                val response = suscriptorService.guardarSuscriptor(suscriptor)
                if (response.isSuccessful) {
                    // El suscriptor se guardó correctamente
                    // Aquí puedes actualizar un LiveData para notificar a la UI si es necesario
                    println("Éxito: ${response.body()}")
                } else {
                    // Hubo un error en la respuesta del servidor
                    println("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Hubo un error en la llamada de red
                println("Excepción: ${e.message}")
            }
        }
    }

    fun actualizarSuscriptor(id: String, suscriptor: SuscriptorRequest) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val response = suscriptorService.actualizarSuscriptor(id, suscriptor)
                if (response.isSuccessful) {
                    _mensaje.value = "Suscriptor actualizado con éxito."
                    obtenerSuscriptores() // Actualiza la lista
                } else {
                    _mensaje.value = "Error al actualizar: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _mensaje.value = "Excepción: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun eliminarSuscriptor(id: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val response = suscriptorService.eliminarSuscriptor(id)
                if (response.isSuccessful) {
                    _mensaje.value = "Suscriptor eliminado con éxito."
                    obtenerSuscriptores() // Actualiza la lista
                } else {
                    _mensaje.value = "Error al eliminar: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _mensaje.value = "Excepción: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}
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

    // --- NUEVOS LIVE DATA PARA VALIDACIÓN ---
    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

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

    // --- FUNCIÓN DE VALIDACIÓN CORREGIDA ---
    // Ahora acepta un SuscriptorRequest para coincidir con `guardarSuscriptor`
    private fun validarCamposSuscriptor(suscriptor: SuscriptorRequest): Boolean {
        // Validación 1: Cédula
        if (suscriptor.cedula.isNullOrBlank()) {
            _validationError.value = "El campo Cédula es obligatorio."
            return false
        }
        if (suscriptor.cedula.length > 8 || !suscriptor.cedula.all { it.isDigit() }) {
            _validationError.value = "La cédula debe contener hasta 8 dígitos numéricos."
            return false
        }

        // Validación 2: Número de Teléfono
        if (suscriptor.numeroTelefono.isNullOrBlank()) {
            _validationError.value = "El campo Teléfono es obligatorio."
            return false
        }
        // Un número de teléfono en Venezuela tiene 11 dígitos (ej: 04141234567)
        if (suscriptor.numeroTelefono.length != 11 || !suscriptor.numeroTelefono.all { it.isDigit() }) {
            _validationError.value = "El teléfono debe contener 11 dígitos numéricos (ej: 04141234567)."
            return false
        }

        // Validación 3: Estatus
        if (suscriptor.estatus.isNullOrBlank()){
            _validationError.value = "El campo Estatus es obligatorio."
            return false
        }

        // Validación 4: Operador
        if (suscriptor.operador.isNullOrBlank()){
            _validationError.value = "El campo Operador es obligatorio."
            return false
        }

        // Si todas las validaciones pasan
        _validationError.value = null // Limpiar cualquier error previo
        return true
    }

    // Llama a este método desde tu Fragment para guardar el suscriptor
    fun guardarSuscriptor(suscriptor: SuscriptorRequest) {
        // 1. Validar los campos antes de hacer la llamada a la API
        // Ahora la llamada es válida porque ambos usan SuscriptorRequest
        if (!validarCamposSuscriptor(suscriptor)) {
            return // Detiene la ejecución si la validación falla
        }

        viewModelScope.launch {
            _cargando.postValue(true) // Usamos postValue si se llama desde otro hilo, value es seguro aquí.
            try {
                val response = suscriptorService.guardarSuscriptor(suscriptor)
                if (response.isSuccessful) {
                    _mensaje.postValue("Suscriptor guardado con éxito.")
                    obtenerSuscriptores() // <-- ¡CLAVE! Refrescar la lista.
                } else {
                    val errorMsg = response.errorBody()?.string()
                    _mensaje.postValue("Error al guardar: $errorMsg")
                    println("Error: $errorMsg")
                }
            } catch (e: Exception) {
                _mensaje.postValue("Excepción al guardar: ${e.message}")
                println("Excepción: ${e.message}")
            } finally {
                // No necesitas el finally para el cargando aquí,
                // porque obtenerSuscriptores() ya lo gestiona.
                // `obtenerSuscriptores` ya gestiona el `_cargando.value = false`
            }
        }
    }

    // --- NUEVA FUNCIÓN PARA LIMPIAR EL ERROR ---
    /**
     * Limpia el mensaje de error de validación.
     * Se debe llamar después de mostrar el error en la UI.
     */
    fun limpiarErrorDeValidacion() {
        _validationError.value = null
    }

    // También actualizamos la validación para el método de actualizar
    fun actualizarSuscriptor(id: String, suscriptor: SuscriptorRequest) {
        // Añadimos la validación aquí también
        if (!validarCamposSuscriptor(suscriptor)) {
            return
        }

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
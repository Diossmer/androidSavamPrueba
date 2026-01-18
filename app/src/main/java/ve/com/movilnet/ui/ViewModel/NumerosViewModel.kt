// en ui/viewmodel/NumerosViewModel.kt
package ve.com.movilnet.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ve.com.movilnet.data.Response.NumerosResponse
import ve.com.movilnet.data.repository.NumeroRepository

class NumerosViewModel : ViewModel() {

    private val repository = NumeroRepository()

    // LiveData para la lista completa de números obtenida de la API
    private val _numeros = MutableLiveData<List<NumerosResponse>>()
    val numeros: LiveData<List<NumerosResponse>> get() = _numeros

    // LiveData para manejar el estado de carga (mostrar/ocultar un ProgressBar)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para comunicar errores a la UI
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Variable para almacenar la lista completa y facilitar el filtrado
    private var listaCompleta: List<NumerosResponse> = emptyList()

    /**
     * Inicia la carga de números desde el repositorio.
     * Se debe llamar desde el Fragment.
     */
    fun cargarNumeros() {
        // Usa viewModelScope para lanzar una corrutina segura en el ciclo de vida del ViewModel
        viewModelScope.launch {
            _isLoading.value = true // Inicia la carga
            val resultado = repository.obtenerNumeros()

            if (resultado != null) {
                listaCompleta = resultado
                _numeros.value = listaCompleta // Actualiza el LiveData con los datos
            } else {
                _error.value = "Error al obtener los números. Inténtalo de nuevo."
            }
            _isLoading.value = false // Finaliza la carga
        }
    }

    /**
     * Filtra la lista de números según una consulta.
     * No realiza una nueva llamada a la API, trabaja con la lista ya cargada.
     */
    fun filtrarLista(query: String) {
        val textoBusqueda = query.lowercase().trim()

        if (textoBusqueda.isEmpty()) {
            _numeros.value = listaCompleta // Muestra la lista completa si no hay búsqueda
        } else {
            val listaFiltrada = listaCompleta.filter { numero ->
                // Asumiendo que NumerosResponse tiene campos como 'telefono', 'cedula' y 'fecha'
                // Ajusta los nombres de los campos a los de tu clase NumerosResponse
                (numero.telefono?.contains(textoBusqueda) ?: false) ||
                        (numero.cedula?.lowercase()?.contains(textoBusqueda) ?: false) ||
                        (numero.fecha?.contains(textoBusqueda) ?: false)
            }
            _numeros.value = listaFiltrada
        }
    }
}

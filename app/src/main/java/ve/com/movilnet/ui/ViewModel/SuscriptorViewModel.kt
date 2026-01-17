package ve.com.movilnet.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ve.com.movilnet.data.Request.SuscriptorRequest
import ve.com.movilnet.data.Services.SuscriptorServices

class SuscriptorViewModel(private val suscriptorService: SuscriptorServices) :
    ViewModel()  {
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
}
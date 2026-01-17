package ve.com.movilnet.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ve.com.movilnet.utils.RetrofitClient // Asegúrate de que este sea el lugar correcto de tu cliente

// Esta clase le enseña al sistema cómo crear tu ViewModel
class SuscriptorViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SuscriptorViewModel::class.java)) {
            // Aquí está la magia: creamos la dependencia (el servicio)
            // y se la pasamos al constructor del ViewModel.
            val service = RetrofitClient.suscriptorServices
            return SuscriptorViewModel(suscriptorService = service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
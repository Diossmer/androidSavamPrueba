// Define que este archivo pertenece al "paquete" o carpeta "ui.Fragments"
package ve.com.movilnet.ui.Fragments

// Importa herramientas necesarias de Android
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment // La herramienta para crear "pegatinas"
import ve.com.movilnet.R // El acceso a todos tus recursos (layouts, imágenes, etc.)

// 1. La Declaración de la Clase
class FragmentNumerosConsulta: Fragment() {

    // 2. El Método que "Dibuja" el Fragmento
    override fun onCreateView(
        inflater: LayoutInflater, // La "Impresora 3D"
        container: ViewGroup?,    // El "Espacio Asignado" en la página
        savedInstanceState: Bundle? // La "Memoria de Estado"
    ): View? { // <- Tiene que devolver el resultado visual

        // 3. La Acción de Inflar (Crear la Vista)
        return inflater.inflate(R.layout.fragment_numero_consulta, container, false)
    }
}

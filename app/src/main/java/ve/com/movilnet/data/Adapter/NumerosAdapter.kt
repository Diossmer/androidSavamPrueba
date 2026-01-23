// en un nuevo archivo llamado NumerosAdapter.kt
package ve.com.movilnet.ui.Adapters // O el paquete que prefieras para tus adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ve.com.movilnet.R
import ve.com.movilnet.data.Response.NumerosResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Define un modelo de datos si aún no lo tienes.
// Puedes crear un archivo NumeroModel.kt
// data class NumeroModel(val cedula: String, val fecha: String, val telefono: String)

class NumerosAdapter(
    private var numeros: List<NumerosResponse> // La lista completa de números
    //private val onEditClick: (NumerosResponse) -> Unit // Lambda para manejar el clic en "Editar"
) : RecyclerView.Adapter<NumerosAdapter.NumeroViewHolder>() {

    // Contenedor de vistas para cada item de la lista
    class NumeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOperador: TextView = itemView.findViewById(R.id.tvOperador)
        val tvDateNumeros: TextView = itemView.findViewById(R.id.tvDateNumeros)
        val tvNumeros: TextView = itemView.findViewById(R.id.tvNumeros)
        //val btnEditar: Button = itemView.findViewById(R.id.btnMostrarDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumeroViewHolder {
        // Infla (crea) la vista de cada fila usando tu layout item_numero.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_numero, parent, false)
        return NumeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: NumeroViewHolder, position: Int) {
        // Vincula los datos del elemento actual con las vistas del ViewHolder
        val numero = numeros[position]

        // --- INICIO DE LA SOLUCIÓN ---

        // 1. Define los formateadores
        val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a") // Formato deseado

        // 2. Procesa la fecha de forma segura
        val fechaFormateada = try {
            // Parsea la fecha que viene de la API (asume que está en UTC o con zona)
            val zonedDateTimeUtc = ZonedDateTime.parse(numero.fecha, inputFormatter)

            // Define la zona horaria de Venezuela
            val venezuelaZoneId = ZoneId.of("America/Caracas")

            // Convierte la fecha a la zona horaria de Venezuela
            val zonedDateTimeVzla = zonedDateTimeUtc.withZoneSameInstant(venezuelaZoneId)

            // Formatea la fecha ya convertida para mostrarla
            zonedDateTimeVzla.format(outputFormatter)
        } catch (_: Exception) {
            // Si la fecha es nula, inválida o tiene un formato inesperado,
            // muestra la cadena original para no dejar el campo vacío.
            numero.fecha
        }
        // --- FIN DE LA SOLUCIÓN ---

        holder.tvOperador.text = numero.cedula // Asumiendo que tvOperador es para la cédula
        // --- INICIO DE LA CORRECCIÓN ---
        // Usa los nombres de campo correctos del JSON que muestras
        holder.tvDateNumeros.text = fechaFormateada // Cambiado de numero.fecha
        holder.tvNumeros.text = numero.telefono         // Cambiado de numero.telefono
        // --- FIN DE LA CORRECCIÓN ---

        // Configura el listener para el botón de editar
        //holder.btnEditar.setOnClickListener {
        //    onEditClick(numero)
        //}
    }

    override fun getItemCount(): Int {
        // Devuelve el número total de elementos en la lista
        return numeros.size
    }

    // Función para actualizar la lista que se muestra (usada para el filtro de búsqueda)
    fun updateList(newList: List<NumerosResponse>) {
        numeros = newList
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}

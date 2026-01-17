package ve.com.movilnet.data.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ve.com.movilnet.R
import ve.com.savam.data.models.Suscriptor
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import kotlin.text.lowercase

class SuscriptorAdapter(
    // Añade el listener como parámetro del constructor
    private val onMostrarClickListener: (Suscriptor) -> Unit
) : RecyclerView.Adapter<SuscriptorAdapter.SuscriptorViewHolder>() {

    // Lista que contiene todos los suscriptores (la fuente de verdad)
    private var listaCompletaSuscriptores: List<Suscriptor> = emptyList()
    // Lista que se muestra en la UI (puede ser filtrada)
    private var listaFiltradaSuscriptores: List<Suscriptor> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuscriptorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suscriptor, parent, false)
        return SuscriptorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuscriptorViewHolder, position: Int) {
        val suscriptor = listaFiltradaSuscriptores[position]
        // Pasamos el suscriptor y el listener al ViewHolder
        holder.bind(suscriptor, onMostrarClickListener)
    }

    override fun getItemCount(): Int = listaFiltradaSuscriptores.size

    // Función para actualizar la lista de datos en el adaptador
    fun actualizarLista(nuevaLista: List<Suscriptor>) {
        listaCompletaSuscriptores = nuevaLista
        listaFiltradaSuscriptores = nuevaLista
        notifyDataSetChanged()
    }

    // Función para filtrar la lista
    fun filtrar(textoBusqueda: String) {
        listaFiltradaSuscriptores = if (textoBusqueda.isEmpty()) {
            listaCompletaSuscriptores
        } else {
            val busquedaLowerCase = textoBusqueda.lowercase()
            listaCompletaSuscriptores.filter { suscriptor ->
                val cedulaMatch = suscriptor?.cedula?.lowercase()?.contains(busquedaLowerCase) ?: false
                val telefonoMatch = suscriptor?.numeroTelefono?.lowercase()?.contains(busquedaLowerCase) ?: false
                val estatusMatch = suscriptor?.estatus?.lowercase()?.contains(busquedaLowerCase) ?: false
                val operadorMatch = suscriptor?.operador?.lowercase()?.contains(busquedaLowerCase) ?: false
                val fechaMatch = suscriptor?.fecha?.toString()?.lowercase()?.contains(busquedaLowerCase) ?: false

                cedulaMatch || telefonoMatch || estatusMatch || operadorMatch || fechaMatch
            }
        }
        notifyDataSetChanged()
    }


    // --- ViewHolder CORREGIDO ---
    class SuscriptorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 1. Declaramos las vistas que SÍ existen en tu XML
        private val tvFecha: TextView = itemView.findViewById(R.id.tvDateSuscriptor)
        private val tvCedula: TextView = itemView.findViewById(R.id.tvCedulaSuscriptor)
        private val tvOperador: TextView = itemView.findViewById(R.id.tvOperadorSuscriptor)
        private val btnMostrar: Button = itemView.findViewById(R.id.btnMostrarDetalle)

        // Formateadores de fecha para reutilizar
        private val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
        private val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        fun bind(suscriptor: Suscriptor, listener: (Suscriptor) -> Unit) {

            // Formateadores para convertir la fecha
            val inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a") // Formato deseado

            // Formateamos la fecha de manera segura y la convertimos a la zona horaria de Venezuela
            val fechaFormateada = try {
                // 1. Parsear la fecha que viene de la API (en UTC)
                val zonedDateTimeUtc = ZonedDateTime.parse(suscriptor.fecha, inputFormatter)

                // 2. Definir la zona horaria de Venezuela
                val venezuelaZoneId = ZoneId.of("America/Caracas")

                // 3. Convertir la fecha de UTC a la zona horaria de Venezuela
                val zonedDateTimeVzla = zonedDateTimeUtc.withZoneSameInstant(venezuelaZoneId)

                // 4. Formatear la fecha ya convertida para mostrarla
                zonedDateTimeVzla.format(outputFormatter)
            } catch (e: Exception) {
                suscriptor.fecha ?: "No disponible" // Fallback si la fecha es nula o inválida
            }

            // 2. Asignamos los datos del suscriptor a las vistas correctas
            tvCedula.text = "Cédula: ${suscriptor?.cedula ?: "N/A"}"
            tvOperador.text = "Operador: ${suscriptor?.operador ?: "N/A"}"

            // 3. Formateamos la fecha para que sea legible

            tvFecha.text = try {
                "Fecha: $fechaFormateada"
            } catch (e: Exception) {
                // Si la fecha es nula o tiene un formato inválido, mostramos un placeholder
                "Fecha: ${suscriptor.fecha ?: "No disponible"}"
            }

            // 4. Configuramos el listener del botón
            btnMostrar.setOnClickListener { listener(suscriptor) }
        }
    }
}
package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels // CAMBIO: Asegúrate de que este import exista
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ve.com.movilnet.R
import ve.com.movilnet.data.Adapter.SuscriptorAdapter
import ve.com.movilnet.ui.ViewModel.SuscriptorViewModel
import ve.com.movilnet.ui.ViewModel.SuscriptorViewModelFactory // CAMBIO: Importa tu Factory
import ve.com.savam.data.models.Suscriptor
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.format

class SuscriptorFragment : Fragment() {

    // CAMBIO: Aquí es donde usas tu Factory.
    // Esta es la forma moderna y recomendada de inicializar un ViewModel.
    private val viewModel: SuscriptorViewModel by viewModels {
        SuscriptorViewModelFactory()
    }

    // Ya no necesitas el lateinit, la línea de arriba se encarga de todo.
    // private lateinit var viewModel: SuscriptorViewModel // BORRAR ESTA LÍNEA

    private lateinit var suscriptorAdapter: SuscriptorAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnMostrar: Button
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_suscriptor, container, false)

        // Inicializar vistas (esto está bien como está)
        recyclerView = view.findViewById(R.id.recyclerViewSuscriptores)
        progressBar = view.findViewById(R.id.progressBar)
        btnMostrar = view.findViewById(R.id.btnMostrarLista)
        searchView = view.findViewById(R.id.searchView)

        // El ViewModel ya se crea automáticamente gracias a `by viewModels`
        // por lo que no necesitas hacer nada más aquí.
        setupRecyclerView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CAMBIO: Ahora que el ViewModel está garantizado, activamos todo.
        setupObservers()
        setupListeners()

        // CAMBIO: Realizamos la primera llamada para cargar los datos.
        viewModel.obtenerSuscriptores()

        // BORRA el mensaje temporal, ya no es necesario.
        // Toast.makeText(context, "TODO: Conecta tu ViewModelFactory", Toast.LENGTH_LONG).show()
    }

    private fun setupRecyclerView() {
        // Pasamos la lambda que maneja el clic al constructor del adaptador
        suscriptorAdapter = SuscriptorAdapter { suscriptor ->
            mostrarDialogoDetalle(suscriptor)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = suscriptorAdapter
        }
    }

    private fun setupListeners() {
        // Botón para refrescar la lista manualmente
        btnMostrar.setOnClickListener {
            viewModel.obtenerSuscriptores()
        }

        // Listener para el SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                suscriptorAdapter.filtrar(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupObservers() {
        // CAMBIO: Usaremos `viewLifecycleOwner` directamente en el observador para más seguridad.
        viewModel.suscriptores.observe(viewLifecycleOwner) { lista ->
            // Cuando la lista se actualiza desde el ViewModel, la pasamos al adaptador
            lista?.let {
                suscriptorAdapter.actualizarLista(it)
                // Opcional: aplicar filtro actual si ya había una búsqueda
                suscriptorAdapter.filtrar(searchView.query.toString())
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { estaCargando ->
            progressBar.visibility = if (estaCargando) View.VISIBLE else View.GONE
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // --- NUEVO OBSERVADOR PARA ERRORES DE VALIDACIÓN ---
        viewModel.validationError.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Muestra el error de validación específico
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()

                // Limpia el error en el ViewModel para que no se muestre de nuevo
                viewModel.limpiarErrorDeValidacion()
            }
        }
    }

    // Esta función ya está correcta y no necesita cambios.
    private fun mostrarDialogoDetalle(suscriptor: Suscriptor) {
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

        // CAMBIO: Eliminamos ".data" de todas las llamadas
        val detalle = StringBuilder().apply {
            append("Cédula: ${suscriptor.cedula ?: "No disponible"}\n\n")
            append("Teléfono: ${suscriptor.numeroTelefono ?: "No disponible"}\n\n")
            append("Estatus: ${suscriptor.estatus ?: "No disponible"}\n\n")
            append("Operador: ${suscriptor.operador ?: "No disponible"}\n\n")
            append("Fecha de Registro: $fechaFormateada\n\n")
            append("WhatsApp: ${if (suscriptor.whatsapp == true) "Sí" else "No"}\n\n")
            append("Telegram: ${if (suscriptor.telegram == true) "Sí" else "No"}")
        }.toString()

        AlertDialog.Builder(requireContext())
            .setTitle("Detalles del Suscriptor")
            .setMessage(detalle)
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

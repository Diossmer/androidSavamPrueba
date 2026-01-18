package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import ve.com.movilnet.R
import ve.com.movilnet.data.Request.SuscriptorRequest // Importar el modelo de Request
import ve.com.movilnet.ui.ViewModel.SuscriptorViewModel // Importar el ViewModel
import ve.com.movilnet.ui.ViewModel.SuscriptorViewModelFactory
import java.util.Date

class SuscriptorBottomSheetFragment : BottomSheetDialogFragment() {

    private var numeroTelefono: String? = null
    private var operador: String? = null
    private var estatus: String? = null
    private var tieneWhatsappInicial: Boolean = false
    private var tieneTelegramInicial: Boolean = false

    // 1. Inyectar el ViewModel usando la delegación de KTX
    private val suscriptorViewModel: SuscriptorViewModel by activityViewModels {
        SuscriptorViewModelFactory() // <-- ¡Este es el cambio clave!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numeroTelefono = it.getString(ARG_NUMERO)
            operador = it.getString(ARG_CEDULA)
            estatus = it.getString(ARG_ESTATUS)
            tieneWhatsappInicial = it.getBoolean(ARG_WHATSAPP)
            tieneTelegramInicial = it.getBoolean(ARG_TELEGRAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_suscriptor_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextNumero = view.findViewById<TextInputEditText>(R.id.editTextNumeroSuscriptor)
        val editTextCedula = view.findViewById<TextInputEditText>(R.id.editTextCedulaSuscriptor)
        val switchWhatsapp = view.findViewById<SwitchMaterial>(R.id.switchWhatsapp)
        val switchTelegram = view.findViewById<SwitchMaterial>(R.id.switchTelegram)
        val buttonGuardar = view.findViewById<MaterialButton>(R.id.buttonGuardarSuscriptor)

        editTextNumero.setText(numeroTelefono)
        switchWhatsapp.isChecked = tieneWhatsappInicial
        switchTelegram.isChecked = tieneTelegramInicial

        // 2. Lógica actualizada del OnClickListener
        buttonGuardar.setOnClickListener {
            val cedulaSuscriptor = editTextCedula.text.toString().trim()
            val tieneWhatsapp = switchWhatsapp.isChecked
            val tieneTelegram = switchTelegram.isChecked

            // Asegurarnos de que tenemos los datos necesarios para enviar
            if (operador == null) {
                Toast.makeText(context, "Error: Faltan datos esenciales (cedula de Suscriptor u operador)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Crear el objeto SuscriptorRequest para enviar a la API
            val suscriptorRequest = SuscriptorRequest(
                id = null,
                numeroTelefono = numeroTelefono,
                estatus = estatus,
                operador = operador,
                fecha = Date(), // Retrofit se encargará de serializarlo a String
                whatsapp = tieneWhatsapp,
                telegram = tieneTelegram,
                cedula = cedulaSuscriptor.ifBlank { null }
            )

            // 4. Llamar al método del ViewModel para guardar los datos
            suscriptorViewModel.guardarSuscriptor(suscriptorRequest)

            // (Opcional) Puedes observar un LiveData del ViewModel para saber cuándo se guardó
            // y mostrar un Toast o cerrar el fragmento. Por ahora, lo hacemos inmediatamente.

            Toast.makeText(context, "Guardando suscriptor...", Toast.LENGTH_SHORT).show()

            dismiss() // Cerrar el BottomSheet
        }
    }

    companion object {
        const val TAG = "SuscriptorBottomSheet"

        private const val ARG_NUMERO = "arg_numero"
        private const val ARG_CEDULA = "arg_cedula"
        private const val ARG_ESTATUS = "arg_estatus"
        private const val ARG_WHATSAPP = "arg_whatsapp"
        private const val ARG_TELEGRAM = "arg_telegram"

        // 5. Corregido: Pasar los booleanos directamente sin negarlos
        fun newInstance(operador: String, numero: String, estatus: String?, tieneWhatsapp: Boolean, tieneTelegram: Boolean): SuscriptorBottomSheetFragment {
            val fragment = SuscriptorBottomSheetFragment()
            val args = Bundle().apply {
                putString(ARG_NUMERO, numero)
                putString(ARG_ESTATUS, estatus)
                putString(ARG_CEDULA, operador)
                putBoolean(ARG_WHATSAPP, tieneWhatsapp) // Corregido
                putBoolean(ARG_TELEGRAM, tieneTelegram) // Corregido
            }
            fragment.arguments = args
            return fragment
        }
    }
}

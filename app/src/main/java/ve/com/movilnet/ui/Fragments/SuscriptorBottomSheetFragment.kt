package ve.com.movilnet.ui.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import ve.com.movilnet.R
import ve.com.savam.data.models.Suscriptor
import java.util.Date

class SuscriptorBottomSheetFragment : BottomSheetDialogFragment() {

    private var numeroTelefono: String? = null
    private var estatus: String? = null
    private var tieneWhatsappInicial: Boolean = false
    private var tieneTelegramInicial: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numeroTelefono = it.getString(ARG_NUMERO)
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

        buttonGuardar.setOnClickListener {
            val cedulaSuscriptor = editTextCedula.text.toString().trim()
            val tieneWhatsapp = switchWhatsapp.isChecked
            val tieneTelegram = switchTelegram.isChecked

            val sharedPreferences = requireActivity().getSharedPreferences("preferencias_usuario", Context.MODE_PRIVATE)
            val operador = sharedPreferences.getString("cedula", null)

            val suscriptor = Suscriptor(
                id = numeroTelefono ?: "",
                numeroTelefono = numeroTelefono,
                estatus = estatus,
                operador = operador,
                fecha = Date(),
                whatsapp = tieneWhatsapp,
                telegram = tieneTelegram,
                cedula = if (cedulaSuscriptor.isNotBlank()) cedulaSuscriptor else null
            )
            
            // Aquí puedes hacer lo que necesites con el objeto suscriptor, como enviarlo a una API.
            // Por ahora, solo mostramos un Toast.
            
            val mensaje = """
                Guardando Suscriptor:
                Número: ${suscriptor.numeroTelefono}
                Cédula: ${suscriptor.cedula ?: "No especificada"}
                Estatus: ${suscriptor.estatus}
                Operador: ${suscriptor.operador}
                WhatsApp: ${suscriptor.whatsapp}
                Telegram: ${suscriptor.telegram}
            """.trimIndent()

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()

            dismiss()
        }
    }

    companion object {
        const val TAG = "SuscriptorBottomSheet"

        private const val ARG_NUMERO = "arg_numero"
        private const val ARG_ESTATUS = "arg_estatus"
        private const val ARG_WHATSAPP = "arg_whatsapp"
        private const val ARG_TELEGRAM = "arg_telegram"

        fun newInstance(numero: String, estatus: String?, tieneWhatsapp: Boolean, tieneTelegram: Boolean): SuscriptorBottomSheetFragment {
            val fragment = SuscriptorBottomSheetFragment()
            val args = Bundle().apply {
                putString(ARG_NUMERO, numero)
                putString(ARG_ESTATUS, estatus)
                putBoolean(ARG_WHATSAPP, !tieneWhatsapp)
                putBoolean(ARG_TELEGRAM, !tieneTelegram)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

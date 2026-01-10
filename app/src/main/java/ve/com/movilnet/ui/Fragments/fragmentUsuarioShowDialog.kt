package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ve.com.movilnet.R
import ve.com.movilnet.ui.viewmodel.UsuarioViewModel
import ve.com.savam.data.models.Usuario

class fragmentUsuarioShowDialog: DialogFragment() {
    private val viewModel: UsuarioViewModel by activityViewModels()

    // Vistas que vamos a rellenar
    private lateinit var title: TextView
    private lateinit var nombre: TextView
    private lateinit var apellido: TextView
    private lateinit var cedula: TextView
    private lateinit var correo: TextView
    private lateinit var rol: TextView // Usaremos un TextView para el rol
    private lateinit var btnCancelar: Button

    override fun onResume() {
        super.onResume()
        // Para que ocupe todo el ancho
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Usa tu nuevo layout (voy a suponer que se llama fragment_usuario_show_dialog.xml)
        return inflater.inflate(R.layout.fragment_usuario_show_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar vistas
        title = view.findViewById(R.id.dialogTitleShow)
        nombre = view.findViewById(R.id.textNombre) // Asegúrate que los IDs coincidan con tu XML
        apellido = view.findViewById(R.id.textApellido)
        cedula = view.findViewById(R.id.textCedula)
        correo = view.findViewById(R.id.textCorreo)
        rol = view.findViewById(R.id.textRol)
        btnCancelar = view.findViewById(R.id.btn_cancelar)

        // 2. Observar los datos del usuario seleccionado
        viewModel.usuarioSeleccionado.observe(viewLifecycleOwner) { usuario ->
            if (usuario != null) {
                populateInfo(usuario)
            }
        }

        // 3. Obtener el ID y pedir los datos
        val userId = arguments?.getString("USER_ID")
        if (userId != null) {
            viewModel.mostrarUsuario(userId)
        }

        // 4. Configurar el botón de cerrar
        btnCancelar.setOnClickListener {
            dismiss()
        }
    }

    private fun populateInfo(usuario: Usuario) {
        title.text = "${usuario.nombre} ${usuario.apellido}"
        nombre.text = usuario.nombre ?: "No disponible"
        apellido.text = usuario.apellido ?: "No disponible"
        cedula.text = usuario.cedula ?: "No disponible"
        correo.text = usuario.correo ?: "No disponible"
        rol.text = usuario.roles?.nombre ?: "Sin rol asignado"
    }

    // El companion object para crearlo de forma segura
    companion object {
        fun newInstance(userId: String): fragmentUsuarioShowDialog {
            val fragment = fragmentUsuarioShowDialog()
            val args = Bundle()
            args.putString("USER_ID", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
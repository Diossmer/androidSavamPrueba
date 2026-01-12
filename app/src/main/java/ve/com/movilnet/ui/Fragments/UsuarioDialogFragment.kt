package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ve.com.movilnet.R
import ve.com.movilnet.ui.viewmodel.UsuarioViewModel
import ve.com.savam.data.models.Roles
import ve.com.savam.data.models.Usuario

class UsuarioDialogFragment : DialogFragment() {
    private val viewModel: UsuarioViewModel by activityViewModels()
    // 1. DECLARAR LAS VISTAS COMO PROPIEDADES DE LA CLASE
    // Esto es más limpio que declararlas dentro de onViewCreated.
    private lateinit var title: TextView
    private lateinit var nombre: EditText
    private lateinit var apellido: EditText
    private lateinit var cedula: EditText
    private lateinit var correo: EditText
    private lateinit var password: EditText
    private lateinit var spinnerRoles: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    // Propiedades para manejar el estado del diálogo
    private var usuarioToEdit: Usuario? = null
    private lateinit var rolesAdapter: ArrayAdapter<String>
    private var rolesList: List<Roles> = emptyList()

    override fun onResume() {
        super.onResume()
        // Le dice a la ventana del diálogo que ocupe todo el ancho del padre
        // y que la altura se ajuste al contenido.
        // Esta es la magia: ajusta el ancho de la ventana del diálogo
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Simplemente infla la vista.
        return inflater.inflate(R.layout.fragment_usuario_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Organizamos el código en funciones claras
        initializeViews(view)
        setupSpinner()
        setupObservers()
        setupClickListeners()

        // Decidimos si es modo CREACIÓN o EDICIÓN
        val userId = arguments?.getString("USER_ID")
        if (userId != null) {
            // MODO EDICIÓN:
            title.text = "Editar Usuario"
            btnGuardar.text = "Actualizar"
            // Pedimos al ViewModel que cargue los datos del usuario.
            // El observador se encargará del resto.
            viewModel.mostrarUsuario(userId)
        } else {
            // MODO CREACIÓN:
            title.text = "Agregar Usuario"
            // Solo necesitamos cargar la lista de roles para el Spinner.
            viewModel.fetchRoles()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ¡AQUÍ ESTÁ LA SOLUCIÓN!
        // Limpia el LiveData del usuario seleccionado en el ViewModel.
        // Esto asegura que si el diálogo se vuelve a abrir en modo "Agregar",
        // no mostrará los datos del último usuario editado.
        viewModel.limpiarUsuarioSeleccionado()
    }
    private fun initializeViews(view: View) {
        // Centralizamos todos los findViewById en un solo lugar.
        title = view.findViewById(R.id.dialogTitle)
        nombre = view.findViewById(R.id.editTextNombre)
        apellido = view.findViewById(R.id.editTextApellido)
        cedula = view.findViewById(R.id.editTextCedula)
        correo = view.findViewById(R.id.editTextCorreo)
        password = view.findViewById(R.id.editTextPassword)
        spinnerRoles = view.findViewById(R.id.spinnerRoles)
        btnGuardar = view.findViewById(R.id.btn_guardar)
        btnCancelar = view.findViewById(R.id.btn_cancelar)
    }

    private fun setupSpinner() {
        rolesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRoles.adapter = rolesAdapter
    }

    private fun setupObservers() {
        // Observador para la LISTA DE ROLES.
        viewModel.roles.observe(viewLifecycleOwner) { roles ->
            rolesList = roles
            val nombresRoles = roles.map { it.nombre ?: "Sin nombre" }
            rolesAdapter.clear()
            rolesAdapter.addAll(nombresRoles)
            rolesAdapter.notifyDataSetChanged()

            // Si estamos editando un usuario (que ya se cargó),
            // intentamos pre-seleccionar su rol AHORA que el spinner tiene datos.
            usuarioToEdit?.let { selectUserRole(it) }
        }

        // Observador para el USUARIO SELECCIONADO (modo edición).
        viewModel.usuarioSeleccionado.observe(viewLifecycleOwner) { usuario ->
            if (usuario != null) {
                usuarioToEdit = usuario
                populateForm(usuario) // Rellena los campos de texto
                // Ahora que tenemos el usuario, cargamos los roles para poder encontrar el suyo.
                viewModel.fetchRoles()
            }
        }
    }

    private fun populateForm(usuario: Usuario) {
        nombre.setText(usuario.nombre)
        apellido.setText(usuario.apellido)
        cedula.setText(usuario.cedula)
        correo.setText(usuario.correo)
        // La contraseña no se debe pre-rellenar por seguridad.
    }

    private fun selectUserRole(usuario: Usuario) {
        val rolDelUsuario = usuario.roles
        if (rolDelUsuario != null && rolesList.isNotEmpty()) {
            // Esta es la forma simple, segura y moderna de encontrar el índice.
            val rolPosition = rolesList.indexOfFirst { it.id == rolDelUsuario.id }
            if (rolPosition != -1) {
                spinnerRoles.setSelection(rolPosition)
            }
        }
    }

    private fun setupClickListeners() {
        btnCancelar.setOnClickListener {
            dismiss()
        }

        btnGuardar.setOnClickListener {
            val selectedRolePosition = spinnerRoles.selectedItemPosition
            if (selectedRolePosition < 0 || selectedRolePosition >= rolesList.size) {
                (spinnerRoles.selectedView as? TextView)?.error = "Selecciona un rol"
                return@setOnClickListener
            }
            val selectedRoleObject = rolesList[selectedRolePosition]

            // Construimos el objeto Usuario con los datos del formulario.
            val usuarioData = Usuario(
                id = usuarioToEdit?.id,
                nombre = nombre.text.toString(),
                apellido = apellido.text.toString(),
                cedula = cedula.text.toString(),
                correo = correo.text.toString(),
                password = if (password.text.isNotBlank()) password.text.toString() else null,
                roles = selectedRoleObject,
                oficina = "Movilnet", // Puedes hacerlo dinámico si lo necesitas
                estado = "activo"
            )

            // El ViewModel se encarga de decidir si crea o actualiza.
            if (usuarioToEdit == null) {
                viewModel.agregarUsuario(usuarioData)
            } else {
                viewModel.actualizarUsuario(usuarioData)
            }
            dismiss()
        }
    }

    // El companion object se mantiene igual.
    companion object {
        fun newInstance(userId: String? = null): UsuarioDialogFragment {
            val fragment = UsuarioDialogFragment()
            if (userId != null) {
                val args = Bundle()
                args.putString("USER_ID", userId)
                fragment.arguments = args
            }
            return fragment
        }
    }
}

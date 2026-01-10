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
import ve.com.movilnet.R
import ve.com.movilnet.ui.viewmodel.UsuarioViewModel
import ve.com.savam.data.models.Roles
import ve.com.savam.data.models.Usuario

class UsuarioDialogFragment : DialogFragment() {

    private val viewModel: UsuarioViewModel by activityViewModels()
    private var usuarioToEdit: Usuario? = null
    private lateinit var spinnerRoles: Spinner // Referencia al Spinner
    private lateinit var rolesAdapter: ArrayAdapter<String> // Adaptador para el Spinner
    private var rolesList: List<Roles> = emptyList() // Lista para almacenar los roles


    // onResume para ajustar el tamaño del diálogo (opcional pero recomendado)
    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Obten el usuario a editar de los argumentos, si existe
        val usuarioId = arguments?.getString("USER_ID")
        if (usuarioId != null) {
            // Busca el usuario en la lista cacheada del ViewModel
            usuarioToEdit = viewModel.usuarios.value?.find { it.id == usuarioId }
        }
        // Asegúrate de que el nombre del layout es el correcto
        return inflater.inflate(R.layout.fragment_usuario_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a las vistas DENTRO del layout del diálogo
        val title: TextView = view.findViewById(R.id.dialogTitle)
        val nombre: EditText = view.findViewById(R.id.editTextNombre)
        val apellido: EditText = view.findViewById(R.id.editTextApellido)
        val cedula: EditText = view.findViewById(R.id.editTextCedula)
        val correo: EditText = view.findViewById(R.id.editTextCorreo)
        val password: EditText = view.findViewById(R.id.editTextPassword)
        // ¡LA FORMA CORRECTA DE OBTENER EL BOTÓN!
        val btnGuardar: Button = view.findViewById(R.id.btn_guardar)
        val btnCancelar: Button = view.findViewById(R.id.btn_cancelar)
        //val btnGuardar: Button? = activity?.findViewById(R.id.btn_guardar_usuario) // Necesitarás añadir este botón a tu layout principal

        // --- INICIALIZAR SPINNER ---
        spinnerRoles = view.findViewById(R.id.spinnerRoles)
        rolesAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRoles.adapter = rolesAdapter

        // Observar la lista de roles desde el ViewModel
        viewModel.roles.observe(viewLifecycleOwner) { roles ->
            rolesList = roles
            // --- LÍNEA CORREGIDA ---
            val nombresRoles = roles.map { rol ->
                rol.nombre ?: "Sin nombre"
            }
            rolesAdapter.clear()
            rolesAdapter.addAll(nombresRoles)
            rolesAdapter.notifyDataSetChanged()

            // Si estamos editando, pre-seleccionar el rol del usuario
            // Obtiene el primer rol de la lista, si existe.
            usuarioToEdit?.roles?.let { rolActual ->
                val position = rolesList.indexOfFirst { it.id == rolActual.id }
                if (position != -1) {
                    spinnerRoles.setSelection(position)
                }
            }
        }

        // Pedir al ViewModel que cargue los roles
        viewModel.fetchRoles()

        // Rellenar datos si es para editar
        if (usuarioToEdit != null) {
            title.text = "Editar Usuario"
            btnGuardar.text = "Actualizar" // Cambiamos el texto del botón
            nombre.setText(usuarioToEdit?.nombre)
            apellido.setText(usuarioToEdit?.apellido)
            cedula.setText(usuarioToEdit?.cedula)
            correo.setText(usuarioToEdit?.correo)
            // La contraseña no se suele pre-rellenar por seguridad
        } else {
            title.text = "Agregar Usuario"
        }
        // Listener para el botón Cancelar
        btnCancelar.setOnClickListener {
            dismiss() // Simplemente cierra el diálogo
        }
        // Listener para el botón Guardar
        btnGuardar.setOnClickListener {
            // Validación básica (puedes mejorarla)
            if (nombre.text.isBlank() || apellido.text.isBlank()) {
                nombre.error = "El nombre es requerido"
                return@setOnClickListener
            }
            /*
            val usuarioData = Usuario(
                id = usuarioToEdit?.id, // Será null si es nuevo, o tendrá valor si es edición
                nombre = nombre.text.toString(),
                apellido = apellido.text.toString(),
                cedula = cedula.text.toString(),
                correo = correo.text.toString(),
                password = password.text.toString(),
                // Rellena los demás campos si son necesarios
                // oficina = null, estado = null, roles = null
                // Rellena los demás campos si son necesarios
                oficina = usuarioToEdit?.oficina, // Mantén los datos existentes si es una edición
                estado = usuarioToEdit?.estado,
                roles = usuarioToEdit?.roles
            )
             */
            /*
            if (usuarioToEdit == null) {
                viewModel.agregarUsuario(nuevoUsuario)
            } else {
                viewModel.actualizarUsuario(nuevoUsuario)
            }
            dismiss() // Cierra el dialog
            */
            // --- OBTENER ROL SELECCIONADO ---
            val selectedRolePosition = spinnerRoles.selectedItemPosition
            if (selectedRolePosition < 0 || selectedRolePosition >= rolesList.size) {
                // Mostrar error si no se ha seleccionado un rol
                (spinnerRoles.selectedView as? TextView)?.error = "Selecciona un rol"
                return@setOnClickListener
            }
            // Obtienes el objeto Rol completo seleccionado en el Spinner
            val selectedRoleObject = rolesList[selectedRolePosition]

            val usuarioData = Usuario(
                id = usuarioToEdit?.id,
                nombre = nombre.text.toString(),
                apellido = apellido.text.toString(),
                cedula = cedula.text.toString(),
                correo = correo.text.toString(),
                password = if (password.text.isNotBlank()) password.text.toString() else null,

                // SOLUCIÓN: Adjuntamos el objeto 'Roles' completo.
                // Ya no es una lista.
                roles = selectedRoleObject,

                oficina = "Movilnet",
                estado = "activo"
            )

            // Llamas al ViewModel con el objeto 'Usuario' correcto.
            if (usuarioToEdit == null) {
                viewModel.agregarUsuario(usuarioData)
            } else {
                viewModel.actualizarUsuario(usuarioData)
            }
            dismiss() // Cierra el diálogo después de la acción
        }
    }

    companion object {
        fun newInstance(userId: String? = null): UsuarioDialogFragment {
            val fragment = UsuarioDialogFragment()
            // Pasamos el ID del usuario a editar a través de argumentos
            if (userId != null) {
                val args = Bundle()
                args.putString("USER_ID", userId)
                fragment.arguments = args
            }
            return fragment
        }
    }
}
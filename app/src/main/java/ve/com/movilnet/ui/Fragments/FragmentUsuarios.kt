package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ve.com.movilnet.R
import ve.com.movilnet.data.Adapter.UsuarioAdapter
import ve.com.movilnet.ui.viewmodel.UsuarioViewModel
import ve.com.savam.data.models.Usuario

class FragmentUsuarios : Fragment(), UsuarioAdapter.OnUsuarioClickListener {

    // Inyección del ViewModel usando la librería ktx.
    private val usuarioViewModel: UsuarioViewModel by activityViewModels()
    private lateinit var usuarioAdapter: UsuarioAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var nombreTextViewTitulo: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout para este fragmento.
        return inflater.inflate(R.layout.fragment_usuarios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nombreTextViewTitulo = view.findViewById(R.id.nombreTextView)
        fab = view.findViewById(R.id.fabAgregarUsuario)
        fab.setOnClickListener {
            UsuarioDialogFragment.newInstance().show(parentFragmentManager, "dialog_usuario")
        }

        // Configura el RecyclerView.
        setupRecyclerView(view)

        // Observa los cambios en los datos y el estado de la UI.
        observeViewModel()

        // Pide al ViewModel que cargue los datos.
        usuarioViewModel.fetchUsuarios()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewUsuarios)
        // Inicializa el adaptador con una lista vacía.// Pasa 'this' como listener al adaptador
        usuarioAdapter = UsuarioAdapter(mutableListOf(), this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = usuarioAdapter
    }

    private fun observeViewModel() {
        // --- ESTE ES EL OBSERVADOR CLAVE ---
        usuarioViewModel.loggedInUser.observe(viewLifecycleOwner, Observer { usuarioLogueado ->
            if (usuarioLogueado != null) {
                // Cuando el ViewModel nos diga quién es el usuario, actualizamos el TextView
                nombreTextViewTitulo.text = usuarioLogueado.roles?.nombre ?: "Sin rol"
            } else {
                nombreTextViewTitulo.text = "Bienvenido" // O algún otro texto por defecto
            }
        })

        // Observador para la lista de usuarios.
        usuarioViewModel.usuarios.observe(viewLifecycleOwner, Observer { usuarios ->
            usuarios?.let {
                // Cuando llegan nuevos datos, actualiza el adaptador.
                usuarioAdapter.updateData(it)
            }
        })

        // Observador para mensajes de error.
        usuarioViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMsg ->
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        })

        // Observador para el estado de carga (puedes mostrar/ocultar un ProgressBar).
        usuarioViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            // if (isLoading) { /* Mostrar ProgressBar */ } else { /* Ocultar ProgressBar */ }
        })
    }

    override fun onShowClick(usuario: Usuario) {
        usuario.id?.let { userId ->
            fragmentUsuarioShowDialog.newInstance(usuario.id)
                .show(parentFragmentManager, "dialog_usuario_show")
        }
    }

    override fun onEditClick(usuario: Usuario) {
        // Muestra el dialog para editar, pasando el ID del usuario
        UsuarioDialogFragment.newInstance(usuario.id)
            .show(parentFragmentManager, "dialog_usuario_edit")
    }

    override fun onDeleteClick(usuario: Usuario) {
        // Muestra un dialog de confirmación antes de borrar
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar a ${usuario.nombre} ${usuario.apellido}?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                usuario.id?.let {
                    usuarioViewModel.eliminarUsuario(it)
                }
            }
            .show()
    }
}
package ve.com.movilnet.data.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ve.com.movilnet.R
import ve.com.savam.data.models.Usuario

class UsuarioAdapter(
    private var usuarios: MutableList<Usuario>,
    private val listener: OnUsuarioClickListener
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {
    /**
     *     Pasos de los fragment
     *     se crea el crud en viewModel
     *     se crea el fragment
     *     y por ultimo se junta en el adapter el viewmodel y el fragment
     */
    // Interface para manejar los clics
    interface OnUsuarioClickListener {
        fun onEditClick(usuario: Usuario)
        fun onDeleteClick(usuario: Usuario)
    }

    // El ViewHolder contiene las vistas (TextViews, etc.) para cada item.
    // Se corresponde con tu layout 'recycle_view_usuario.xml'.
    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTextView: TextView = itemView.findViewById(R.id.id)
        val nombreTextView: TextView = itemView.findViewById(R.id.nombre)
        val apellidoTextView: TextView = itemView.findViewById(R.id.apellido)
        val correoTextView: TextView = itemView.findViewById(R.id.correo)

        //val editButton: Button = itemView.findViewById(R.id.btn_editar) // Añadí un ID al botón
        val editButton: Button = itemView.findViewById(R.id.btn_editar)
        val deleteButton: Button =
            itemView.findViewById(R.id.btn_eliminar) // Necesitarás añadir este botón
    }

    // Crea una nueva vista (invocado por el layout manager).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        // Infla (crea) la vista para un item a partir de tu XML.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycle_view_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    // Vincula los datos de un usuario en una posición específica con las vistas del ViewHolder.
    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]

        // Asigna los datos del usuario a los TextViews.
        holder.idTextView.text = usuario.id ?: "N/A"
        holder.nombreTextView.text = usuario.nombre ?: "Sin nombre"
        holder.apellidoTextView.text = usuario.apellido ?: "Sin apellido"
        holder.correoTextView.text = usuario.correo ?: "Sin correo"

        // Aquí puedes añadir listeners para los botones de editar/eliminar
        //holder.editButton.setOnClickListener {
        // Lógica para editar el usuario en la posición 'position'
        //}
        holder.editButton.setOnClickListener {
            listener.onEditClick(usuario)
        }

        holder.deleteButton.setOnClickListener {
            listener.onDeleteClick(usuario)
        }
    }

    // Devuelve el número total de items en la lista.
    override fun getItemCount() = usuarios.size

    // Función para actualizar la lista de usuarios en el adaptador.
    fun updateData(newUsuarios: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(newUsuarios)
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado.
    }
}
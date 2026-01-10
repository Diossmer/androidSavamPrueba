package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName

data class UsuarioRequest(
    @SerializedName("id") val id: String?,
    @SerializedName("oficina") val oficina: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("cedula") val cedula: String?,
    @SerializedName("password") val password: String?,
    // LA CLAVE: El backend espera un campo "roles" que sea una Lista de Strings (los IDs)
    @SerializedName("roles") val roles: List<String>?
)

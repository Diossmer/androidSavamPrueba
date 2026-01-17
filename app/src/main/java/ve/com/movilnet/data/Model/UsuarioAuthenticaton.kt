package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName
import ve.com.movilnet.data.Response.RolesResponse

data class UsuarioAuthenticaton(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("cedula") val cedula: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("roles") val roles: RolesResponse?
)

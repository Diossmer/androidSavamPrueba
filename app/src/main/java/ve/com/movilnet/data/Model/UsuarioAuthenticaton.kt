package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName

data class UsuarioAuthenticaton(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("roles") val roles: String?
)

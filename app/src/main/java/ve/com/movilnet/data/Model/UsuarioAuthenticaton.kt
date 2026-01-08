package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName

data class UsuarioAuthenticaton(
    @SerializedName("_id") val id: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("roles") val roles: String?
)

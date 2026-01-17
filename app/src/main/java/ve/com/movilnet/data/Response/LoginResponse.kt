package ve.com.movilnet.data.Response

import com.google.gson.annotations.SerializedName
import ve.com.movilnet.data.Model.UsuarioAuthenticaton

data class LoginResponse(
    val token: String?,
    val usuario: UsuarioAuthenticaton?,
    @SerializedName("mensaje") val message: String? // Para mensajes de error
)

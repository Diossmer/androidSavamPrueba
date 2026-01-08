package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val token: String?,
    val usuario: UsuarioAuthenticaton?,
    @SerializedName("mensaje") val message: String? // Para mensajes de error
)

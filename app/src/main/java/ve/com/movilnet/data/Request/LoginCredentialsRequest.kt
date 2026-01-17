package ve.com.movilnet.data.Request

import com.google.gson.annotations.SerializedName

data class LoginCredentialsRequest(
    @SerializedName("Usuario")
    val Usuario: String,
    @SerializedName("Password")
    val Password: String
)
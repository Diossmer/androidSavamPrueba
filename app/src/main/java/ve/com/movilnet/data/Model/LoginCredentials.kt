package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName

data class LoginCredentials(
    @SerializedName("Usuario")
    val Usuario: String,
    @SerializedName("Password")
    val Password: String
)
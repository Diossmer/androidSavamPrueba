package ve.com.movilnet.data.Response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("oficina") val oficina: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("cedula") val cedula: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("roles") val roles: List<String>?
)

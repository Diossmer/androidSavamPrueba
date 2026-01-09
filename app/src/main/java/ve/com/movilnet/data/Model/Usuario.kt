package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName

//Model
data class Usuario(
    @SerializedName("id") val id: String?,
    @SerializedName("oficina") val oficina: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("cedula") val cedula: String?,
    @Transient val password: String?,
    @SerializedName("roles") val roles: Roles?
)
package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName

data class Numeros(
    @SerializedName("id") val id: Int,
    @SerializedName("cedula") val cedula: String?,
    @SerializedName("estatus") val estatus: String?,
    @SerializedName("numeroTelefono") val numeroTelefono: String?
)

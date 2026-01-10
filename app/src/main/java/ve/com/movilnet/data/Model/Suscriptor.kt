package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Suscriptor(
    @SerializedName("id") val id: String,
    @SerializedName("numeroTelefono") val numeroTelefono: String?,
    @SerializedName("estatus") val estatus: String?,
    @SerializedName("operador") val operador: String?,
    @SerializedName("fecha") val fecha: Date?,
    @SerializedName("whatsapp") val whatsapp: Boolean?,
    @SerializedName("telegram") val telegram: Boolean?,
    @SerializedName("cedula") val cedula: String?
)

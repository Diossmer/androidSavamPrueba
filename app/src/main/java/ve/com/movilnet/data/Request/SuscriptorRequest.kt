package ve.com.movilnet.data.Request

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SuscriptorRequest(
    @SerializedName("cedula") val cedula: String?,
    @SerializedName("estatus") val estatus: String?,
    @SerializedName("numeroTelefono") val numeroTelefono: String?,
    @SerializedName("whatsapp") val whatsapp: Boolean?,
    @SerializedName("telegram") val telegram: Boolean?,
    @SerializedName("operador") val operador: String?,
    @SerializedName("fecha") val fecha: Date?,
    @SerializedName("id") val id: String,
)
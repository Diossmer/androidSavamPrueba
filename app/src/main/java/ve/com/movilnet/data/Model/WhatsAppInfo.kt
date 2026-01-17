package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName

//data class WhatsAppInfo(
//    @SerializedName("id") val id: Int,
//    @SerializedName("cedula") val cedula: String?,
//    @SerializedName("estatus") val estatus: String?,
//    @SerializedName("numeroTelefono") val numeroTelefono: String?
//)
data class WhatsAppInfo(
    @SerializedName("tiene_whatsapp")
    val tiene_whatsapp: Boolean,

    @SerializedName("titulo_pagina")
    val titulo_pagina: String,

    @SerializedName("fecha_consulta")
    val fecha_consulta: String
)
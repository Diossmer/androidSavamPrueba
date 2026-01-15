package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName

//data class Numeros(
//    @SerializedName("id") val id: Int,
//    @SerializedName("cedula") val cedula: String?,
//    @SerializedName("estatus") val estatus: String?,
//    @SerializedName("numeroTelefono") val numeroTelefono: String?
//)
data class Numeros(
    @SerializedName("numero_ingresado")
    val numero_ingresado: String,

    @SerializedName("numero_formateado")
    val numero_formateado: String,

    @SerializedName("tiene_whatsapp")
    val tiene_whatsapp: Boolean,

    @SerializedName("titulo_pagina")
    val titulo_pagina: String,

    @SerializedName("fecha_consulta")
    val fecha_consulta: String
)
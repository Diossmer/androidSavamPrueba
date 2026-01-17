package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Suscriptor(
    // ESTRUCTURA CORRECTA (refleja el JSON de tu API)
    val id: String?,
    val cedula: String?,
    val estatus: String?,
    val operador: String?,
    val numeroTelefono: String?,
    val fecha: String?,
    val whatsapp: Boolean?,
    val telegram: Boolean?
)

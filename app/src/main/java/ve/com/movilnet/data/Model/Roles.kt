package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName
import ve.com.movilnet.data.Model.Accion

data class Roles(
    @SerializedName("_id") val id: String,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("accion") val accion: List<Accion?>
)


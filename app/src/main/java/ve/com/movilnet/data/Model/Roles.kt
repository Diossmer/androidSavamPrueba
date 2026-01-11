package ve.com.savam.data.models

import com.google.gson.annotations.SerializedName

data class Roles(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("accion") val permisos: List<String?>?
)


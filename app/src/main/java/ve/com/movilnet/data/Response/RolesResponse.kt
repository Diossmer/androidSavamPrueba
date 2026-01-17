package ve.com.movilnet.data.Response

import com.google.gson.annotations.SerializedName

data class RolesResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("accion") val permisos: List<String?>?
)
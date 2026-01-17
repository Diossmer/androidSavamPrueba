package ve.com.movilnet.data.Response

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ve.com.movilnet.data.Response.RolesResponse
import ve.com.savam.data.models.RolesTypeAdapter

//Model
data class UsuarioResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("oficina") val oficina: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("cedula") val cedula: String?,
    @SerializedName("password") val password: String?,
    //@SerializedName("roles") val roles: RolesResponse?
    // --- ¡ANOTACIÓN CLAVE! ---
    @JsonAdapter(RolesTypeAdapter::class)
    val roles: RolesResponse?, // Se queda como un objeto RolesResponse?
)
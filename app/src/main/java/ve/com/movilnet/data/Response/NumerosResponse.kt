package ve.com.movilnet.data.Response

import com.google.gson.annotations.SerializedName

data class NumerosResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("cedula")
    val cedula: String,

    @SerializedName("numero")
    val telefono: String, // Usamos 'telefono' como nombre de variable en Kotlin

    @SerializedName("fechaDeConsulta")
    val fecha: String // Usamos 'fecha' como nombre de variable en Kotlin
)
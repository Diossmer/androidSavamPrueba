package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName
import ve.com.savam.data.Model.Numeros

data class ConsultaResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String, // Corregido de "mensaje" a "message"

    @SerializedName("data")
    val data: Numeros? // ¡Aquí está la clave! Le decimos que espere un objeto "data"
)

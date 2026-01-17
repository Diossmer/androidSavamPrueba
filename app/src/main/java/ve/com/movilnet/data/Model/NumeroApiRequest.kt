package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName

/**
 * Representa el cuerpo de la petición POST para CONSULTAR el estado de un número.
 */
data class NumeroApiRequest(
    @SerializedName("numero")
    val numero: String
)

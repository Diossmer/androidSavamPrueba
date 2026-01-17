package ve.com.movilnet.data.Model

import com.google.gson.annotations.SerializedName


data class ConsultaRequest(

    @SerializedName("cedula")
    val cedula: String?, // La cédula del usuario logueado. No debe ser nula.

    @SerializedName("numero")
    val numero: String?, // El número consultado. Debe ser String para incluir el 0 inicial.

    @SerializedName("fechaDeConsulta")
    val fechaDeConsulta: String? // La fecha de la consulta.
)


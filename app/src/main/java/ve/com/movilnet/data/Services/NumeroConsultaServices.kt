package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ve.com.movilnet.data.Request.ConsultaRequest
import ve.com.movilnet.data.Response.ConsultaResponse
import ve.com.movilnet.data.Request.NumeroApiRequest
import ve.com.movilnet.data.Response.NumerosResponse

interface NumeroConsultaServices {
    @POST("consultar-numeros")
    suspend fun consultarNumero(@Body request: NumeroApiRequest): Response<ConsultaResponse>

    /**
     * Guarda el resultado de una consulta de número en el servidor.
     */
    @POST("numeros") // <-- El nuevo endpoint de tu API
    suspend fun guardarResultadoConsulta(@Body request: ConsultaRequest): Response<Unit> // Response<Unit> si la API no devuelve nada en el cuerpo

    @GET("numeros")
    suspend fun mostrarNumeros(): Response<MutableList<NumerosResponse>>
}
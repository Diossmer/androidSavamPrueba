package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ve.com.movilnet.data.Model.ConsultaRequest
import ve.com.movilnet.data.Model.ConsultaResponse

interface NumeroConsultaServices {
    @POST("api/consultar-whatsapp")
    suspend fun consultarNumero(@Body request: ConsultaRequest): Response<ConsultaResponse>
}
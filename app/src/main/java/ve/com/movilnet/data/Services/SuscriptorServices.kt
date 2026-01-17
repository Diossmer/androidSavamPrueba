package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ve.com.movilnet.data.Request.SuscriptorRequest
import ve.com.movilnet.data.Response.SuscriptorResponse

interface SuscriptorServices {
    /*Guardar Suscriptor*/
    @POST("suscriptor")
    suspend fun guardarSuscriptor(@Body request: SuscriptorRequest): Response<SuscriptorResponse>
}
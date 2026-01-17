package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ve.com.movilnet.data.Request.SuscriptorRequest
import ve.com.movilnet.data.Response.SuscriptorResponse
import ve.com.savam.data.models.Suscriptor

interface SuscriptorServices {
    /*Guardar Suscriptor*/
    @POST("suscriptor")
    suspend fun guardarSuscriptor(@Body request: SuscriptorRequest): Response<SuscriptorResponse>
    /* READ: Obtener todos los Suscriptores */
    @GET("suscriptor")
    suspend fun obtenerSuscriptores(): Response<MutableList<Suscriptor>>
    /* UPDATE: Actualizar un Suscriptor */
    // Asumiendo que necesitas un ID para saber a quién actualizar.
    // La URL podría ser "suscriptor/123"
    @PUT("suscriptor/{id}")
    suspend fun actualizarSuscriptor(
        @Path("id") suscriptorId: String,
        @Body request: SuscriptorRequest
    ): Response<SuscriptorResponse>

    /* DELETE: Eliminar un Suscriptor */
    @DELETE("suscriptor/{id}")
    suspend fun eliminarSuscriptor(@Path("id") suscriptorId: String): Response<Unit> // O el tipo de respuesta que devuelva tu API al eliminar

}
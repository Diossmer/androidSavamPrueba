package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import ve.com.movilnet.data.Model.GenericResponse
import ve.com.movilnet.data.Model.UpdatePasswordRequest

interface UpdatePasswodServices {
    @POST("api/update-password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Body request: UpdatePasswordRequest
    ): Response<GenericResponse>
}
package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ve.com.movilnet.data.Response.LoginResponse
import ve.com.movilnet.data.Response.ProfileResponse
import ve.com.movilnet.data.Request.LoginCredentialsRequest

interface CredentialsServices {
//repositorio/API
    @POST("login")
    suspend fun loginPost(@Body request: LoginCredentialsRequest): Response<LoginResponse>
    @POST("profile")
    suspend fun profile(): Response<MutableList<ProfileResponse>>
    @POST("logout")
    suspend fun logout(): Response<Unit>
}
//marcosps1985@gmail.com
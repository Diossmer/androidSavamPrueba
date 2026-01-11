package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ve.com.movilnet.data.Model.LoginResponse
import ve.com.movilnet.data.Model.ProfileResponse
import ve.com.savam.data.models.LoginCredentials

interface CredentialsServices {

    @POST("login")
    suspend fun loginPost(@Body request: LoginCredentials): Response<LoginResponse>
    @POST("profile")
    suspend fun profile(): Response<MutableList<ProfileResponse>>
    @POST("logout")
    suspend fun logout(): Response<Unit>
}
//marcosps1985@gmail.com
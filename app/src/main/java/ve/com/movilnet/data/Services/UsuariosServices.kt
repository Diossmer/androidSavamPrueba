package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ve.com.movilnet.data.Request.UsuarioRequest
import ve.com.movilnet.data.Response.UsuarioResponse

interface UsuariosServices {
    @GET("usuarios")
    suspend fun listUsuarios(): Response<MutableList<UsuarioResponse>>
    @POST("usuarios")
    suspend fun storeUsuario(@Body usuario: UsuarioRequest): Response<UsuarioResponse> // Recibes un UsuarioResponse, pero envías un UsuarioRequest
    @GET("usuarios/{id}")
    suspend fun showUsuario(@Path("id") id: String): Response<UsuarioResponse>
    @PUT("usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: String, @Body usuario: UsuarioRequest): Response<UsuarioResponse>
    @PATCH("usuarios/{id}")
    suspend fun patchPost(@Path("id") id: String, @Body usuario: UsuarioRequest): Response<UsuarioResponse>
    @DELETE("usuarios/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>
}
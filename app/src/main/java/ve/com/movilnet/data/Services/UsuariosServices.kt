package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import ve.com.savam.data.models.Usuario

interface UsuariosServices {
    @GET("usuarios")
    suspend fun listUsuarios(): Response<MutableList<Usuario>>
    @POST("usuarios")
    suspend fun storeUsuario(@Body request: Usuario): Response<Usuario>
    @GET("usuarios/{id}")
    suspend fun getComments(@Path("id") id: String): Response<MutableList<Usuario>>
    @PATCH("usuarios/{id}")
    suspend fun patchPost(@Path("id") id: String, @Body request: Usuario): Response<Usuario>
    @DELETE("usuarios/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>
}
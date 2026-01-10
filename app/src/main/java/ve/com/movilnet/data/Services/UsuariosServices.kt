package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ve.com.movilnet.data.Model.UsuarioRequest
import ve.com.savam.data.models.Usuario

interface UsuariosServices {
    @GET("usuarios")
    suspend fun listUsuarios(): Response<MutableList<Usuario>>
    @POST("usuarios")
    suspend fun storeUsuario(@Body usuario: UsuarioRequest): Response<Usuario> // Recibes un Usuario, pero envías un UsuarioRequest
    @GET("usuarios/{id}")
    suspend fun getComments(@Path("id") id: String): Response<MutableList<Usuario>>
    @PUT("usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: String, @Body usuario: UsuarioRequest): Response<Usuario>
    //...
    @PATCH("usuarios/{id}")
    suspend fun patchPost(@Path("id") id: String, @Body usuario: UsuarioRequest): Response<Usuario>
    @DELETE("usuarios/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>
}
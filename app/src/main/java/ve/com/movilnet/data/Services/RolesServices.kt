package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.GET
import ve.com.savam.data.models.Roles

interface RolesServices {
    @GET("roles")
    suspend fun listRoles(): Response<MutableList<Roles>>
}
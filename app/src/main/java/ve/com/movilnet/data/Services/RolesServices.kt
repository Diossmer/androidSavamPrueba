package ve.com.movilnet.data.Services

import retrofit2.Response
import retrofit2.http.GET
import ve.com.movilnet.data.Response.RolesResponse

interface RolesServices {
    @GET("roles")
    suspend fun listRoles(): Response<MutableList<RolesResponse>>
}
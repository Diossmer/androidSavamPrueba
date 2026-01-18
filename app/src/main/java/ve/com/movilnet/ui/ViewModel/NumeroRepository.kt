// en app/src/main/java/ve/com/movilnet/data/repository/NumeroRepository.kt
package ve.com.movilnet.data.repository

import android.util.Log
import ve.com.movilnet.data.Response.NumerosResponse
import ve.com.movilnet.data.Services.NumeroConsultaServices
import ve.com.movilnet.utils.RetrofitClient

class NumeroRepository {

    // Obtenemos la instancia del servicio de Retrofit.
    // RetrofitClient.instance debe devolver tu objeto Retrofit configurado.
//    private val apiService: NumeroConsultaServices = RetrofitClient.instance
//        .create(NumeroConsultaServices::class.java)

    /**
     * Llama al endpoint `mostrarNumeros` de la API.
     * Maneja la respuesta y devuelve la lista de números si es exitosa.
     * Devuelve una lista vacía si hay un error para más seguridad.
     */
    suspend fun obtenerNumeros(): List<NumerosResponse> {
        return try {
            val response = RetrofitClient.numeroConsultaServices.mostrarNumeros()
            if (response.isSuccessful) {
                // Si la respuesta es exitosa, devuelve el cuerpo (la lista), o una lista vacía si el cuerpo es nulo.
                response.body() ?: emptyList()
            } else {
                // Si el servidor respondió con un error (ej: 404, 500)
                Log.e("NumeroRepository", "Error en la respuesta: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            // Si ocurrió un error en la llamada (ej: no hay internet, el servidor no responde)
            Log.e("NumeroRepository", "Excepción al obtener números: ", e)
            emptyList()
        }
    }
}

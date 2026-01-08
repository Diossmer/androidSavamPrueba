package ve.com.movilnet.data.Authentication

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que añade el token de autenticación a la cabecera
 * de todas las peticiones que lo requieran.
 *
 * @param sessionManager Una instancia de SessionManager para obtener el token.
 * •SessionManager: Almacén de datos de sesión.
 * •AuthInterceptor: Middleware que modifica las peticiones sobre la marcha.
 * •OkHttp: El motor de red de bajo nivel que hace el trabajo pesado y ejecuta el middleware.
 * •Retrofit: La capa de abstracción de alto nivel que organiza la API de forma limpia y utiliza a OkHttp para ejecutar las llamadas.
 */
class AuthInterceptor(private val sessionManager: SessionManager): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Obtenemos la petición original.
        val originalRequest = chain.request()

        // 2. Recuperamos el token guardado.
        val token = sessionManager.fetchAuthToken()

        // 3. Si no hay token, simplemente continuamos con la petición original.
        //    Esto es útil para las peticiones que no necesitan autenticación (como el login).
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // 4. Si hay un token, creamos una nueva petición añadiendo la cabecera de autorización.
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token") // Formato común "Bearer <token>"
            .build()

        // 5. Dejamos que la nueva petición continúe su camino.
        return chain.proceed(newRequest)
    }

}
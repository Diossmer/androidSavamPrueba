package ve.com.movilnet.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ve.com.movilnet.data.Services.CredentialsServices
import ve.com.movilnet.data.Services.NumeroConsultaServices
import ve.com.movilnet.data.Services.RolesServices
import ve.com.movilnet.data.Services.UsuariosServices

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    // --- 2. CONFIGURACIÓN DEL CLIENTE HTTP (OkHttp) ---
    // Interceptor para poder ver en el logcat las peticiones y respuestas de la API.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // --- 3. CREACIÓN DE UNA ÚNICA INSTANCIA DE RETROFIT ---
    // Se crea una sola vez de forma "perezosa" (lazy).
    // ¡Aquí se inyecta el `gson` personalizado y el `httpClient`!
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // ¡CAMBIO CLAVE! Usa la instancia centralizada.
            .addConverterFactory(GsonConverterFactory.create(GsonProvider.instance)) // <--- USA EL PROVIDER
            .client(httpClient)
            .build()
    }

    // --- 4. CREACIÓN DE LOS SERVICIOS DE LA API ---
    // Todos los servicios se crean a partir de la MISMA instancia de Retrofit,
    // garantizando que todos usen la configuración correcta.

    val usuariosServices: UsuariosServices by lazy {
        retrofit.create(UsuariosServices::class.java)
    }

    val credentialsServices: CredentialsServices by lazy {
        retrofit.create(CredentialsServices::class.java)
    }

    val rolesServices: RolesServices by lazy {
        retrofit.create(RolesServices::class.java)
    }

    val numeroConsultaServices: NumeroConsultaServices by lazy {
        retrofit.create(NumeroConsultaServices::class.java)
    }
}
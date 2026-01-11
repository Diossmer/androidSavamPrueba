package ve.com.movilnet.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ve.com.movilnet.data.Services.CredentialsServices
import ve.com.movilnet.data.Services.RolesServices
import ve.com.movilnet.data.Services.UsuariosServices

object RetrofitClient {
    // URL base de tu API.
    // Si estás probando en el emulador de Android, 10.0.2.2 apunta al localhost de tu máquina.
    //private const val BASE_URL = "http://192.168.1.101:3000/api/" // <-- ¡CAMBIA ESTA URL POR LA DE TU API!
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    // 1. Crea el interceptor de logs
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // BODY muestra toda la info
    }

    // 2. Crea un cliente de OkHttp y añade el interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Creación "perezosa" (lazy) de la instancia de Retrofit.
    // Solo se creará una vez cuando se necesite por primera vez.
    val usuariosServices: UsuariosServices by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para parsear el JSON
            .client(client)
            .build()

        retrofit.create(UsuariosServices::class.java)
    }

    val credentialsServices: CredentialsServices by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para parsear el JSON
            .client(client)
            .build()

        retrofit.create(CredentialsServices::class.java)
    }

    val rolesServices: RolesServices by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        retrofit.create(RolesServices::class.java)
    }
}
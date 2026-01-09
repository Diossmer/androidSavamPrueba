package ve.com.movilnet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ve.com.movilnet.data.Authentication.AuthInterceptor
import ve.com.movilnet.data.Authentication.SessionManager
import ve.com.movilnet.data.Services.CredentialsServices
import ve.com.movilnet.ui.ViewModel.SecondActivity
import ve.com.movilnet.utils.RetrofitClient
import ve.com.savam.data.models.LoginCredentials

class MainActivity : AppCompatActivity() {
    private lateinit var usuarioEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var ingresarButton: Button
    private lateinit var credentialsServices: CredentialsServices

    //declarando la sessionManager
    private lateinit var sessionManager: SessionManager

    // --- 2. DECLARA EL LAUNCHER ---
    // Este es el "oyente" que se quedará esperando un resultado de SecondActivity.
    // Este es el "oyente" que se quedará esperando un resultado de SecondActivity.
    private val secondActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Este bloque se ejecutará CUANDO SecondActivity se cierre y devuelva un resultado.
        if (result.resultCode == RESULT_LOGOUT) {
            // Si el resultado es el que esperamos (logout)...

            // --- ¡CAMBIO CLAVE! Inicia una corrutina para llamar a la API ---
            lifecycleScope.launch {
                try {
                    // 1. Llama a la API para invalidar el token en el servidor
                    val response = RetrofitClient.credentialsServices.logout() // <-- LLAMADA A LA API

                    if (response.isSuccessful) {
                        Log.d("LOGOUT_API", "Token invalidado en el servidor correctamente.")
                    } else {
                        // Opcional: Manejar el caso en que la API falle al hacer logout
                        Log.e("LOGOUT_API", "Error al cerrar sesión en la API: ${response.code()}")
                        // Aunque falle la API, es buena idea continuar con el logout local
                    }
                } catch (e: Exception) {
                    // Opcional: Manejar errores de red o excepciones
                    Log.e("LOGOUT_API", "Excepción al llamar a la API de logout", e)
                } finally {
                    // 2. Realiza el logout local (esto se ejecutará siempre, incluso si la API falla)
                    sessionManager.logout() // <-- Limpia el token y rol del dispositivo
                    Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                    // 3. Limpia los campos de texto
                    usuarioEdit.text.clear()
                    passwordEdit.text.clear()
                }
            }
        }
    }

    // --- 3. AÑADE UNA CONSTANTE PARA EL CÓDIGO DE RESULTADO ---
    companion object {
        const val RESULT_LOGOUT = 100 // Un número único para identificar la acción de logout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        usuarioEdit = findViewById(R.id.usuarioEditText)
        passwordEdit = findViewById(R.id.passwordEditText)
        ingresarButton = findViewById(R.id.ingresarButton)

        //Inicializo la sessionManager
        sessionManager = SessionManager(applicationContext)
        /*
        // Contruyendo el OkHttpClient y Retrofit con el Interceptor
        val authInterceptor = AuthInterceptor(sessionManager)
        //se crea el cliente de OkHttp añadiendo tu interceptor
        val okHttpClient = OkHttpClient.Builder().addInterceptor(authInterceptor).build()
        val retrofit = Retrofit.Builder()
            //.baseUrl("http://10.0.2.2:3000/api/")
            //// Ejemplo si la IP de tu PC es 192.168.100.5 no la del telefono
            .baseUrl("http:/192.168.1.101:3000/api/")
            .client(okHttpClient)// <--------- ¡Aqui esta la magia! usando el cliente de OkHttp personalizado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        */

        usuarioEdit.addTextChangedListener(textWatcher)
        passwordEdit.addTextChangedListener(textWatcher)

        // Inicializa el servicio aquí, una sola vez.
        //credentialsServices = retrofit.create(CredentialsServices::class.java)
        ingresarButton.setOnClickListener {
            login()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- CORRECCIÓN: Llamada a la red dentro de una corrutina ---
    private fun login() {
        // Usa lifecycleScope para lanzar la corrutina. Se cancelará automáticamente
        // cuando el ciclo de vida de la Activity se destruya.
        val usuario = usuarioEdit.text.toString().trim()
        val contrasena = passwordEdit.text.toString().trim()
        val loginRequest = LoginCredentials(usuario, contrasena)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.credentialsServices.loginPost(loginRequest)

                // Aquí puedes verificar la respuesta del servidor
                if (response.isSuccessful) {
                    // Guarda el token al hacer login
                    val loginResponse = response.body()
                    // ASUNCIÓN: Tu modelo LoginResponse ahora tiene "token" y "role"
                    // ej: data class LoginResponse(val token: String, val role: String)
                    if (loginResponse?.token != null && loginResponse.usuario?.roles != null) {
                        sessionManager.saveAuthToken(loginResponse.token)
                        sessionManager.saveUserRole(loginResponse.usuario.roles)
                        // Login exitoso
                        Toast.makeText(this@MainActivity, "Login Exitoso!", Toast.LENGTH_SHORT)
                            .show()
                        // --- 4. MODIFICA CÓMO LANZAS SECOND ACTIVITY ---
                        // Ya no usamos startActivity(intent), sino el launcher.
                        val intent = Intent(this@MainActivity, SecondActivity::class.java)
                        secondActivityLauncher.launch(intent) // <-- ¡CAMBIO CLAVE!
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(
                            this@MainActivity, "ERROR: ${response.code()} - $errorBody",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // El servidor respondió con un error (ej: 401, 404)
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${response.code()} - $errorBody",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                // Cualquier otro error inesperado
                Toast.makeText(
                    this@MainActivity,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("LOGIN_ERROR", "Exception", e)
            }
        }
    }

    /**** WATCHER CODE ****/
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            try {
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
            try {
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            try {
                ingresarButton.isEnabled =
                    !usuarioEdit.text.toString().trim().isEmpty() && !passwordEdit.text.toString()
                        .trim().isEmpty() && passwordEdit.error == null
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
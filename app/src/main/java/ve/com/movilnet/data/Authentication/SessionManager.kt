package ve.com.movilnet.data.Authentication

import android.content.Context
import android.content.SharedPreferences
import ve.com.movilnet.utils.GsonProvider
import ve.com.savam.data.models.Usuario
import androidx.core.content.edit

// Hacemos el constructor privado para forzar el uso del método `getInstance`.
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    // Se crea una única instancia del editor aquí
    //private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Claves para guardar los valores
    companion object {
        private const val PREFS_NAME = "session_prefs"
        const val KEY_AUTH_TOKEN = "token"
        const val KEY_USER_ROLE = "role"
        const val KEY_USER_NAME = "USER_NAME"
        const val KEY_CORREO = "USER_EMAIL"
        const val KEY_JSON = "USER_JSON"

        // 1. Instancia Singleton Volátil
        //    @Volatile garantiza que los cambios en la instancia sean visibles para todos los hilos.
        @Volatile
        private var INSTANCE: SessionManager? = null

        // 2. Método para obtener la instancia (Singleton)
        //    Este es el único punto de entrada para obtener el SessionManager.
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                // Usamos el constructor privado aquí
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // --- MÉTODOS PARA GUARDAR DATOS ---

    fun saveUser(usuarioJson: String) {
        sharedPreferences.edit().putString(KEY_JSON, usuarioJson).apply()
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun saveUserRole(role: String?) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun saveUserName(name: String?) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun saveUserEmail(correo: String?) {
        sharedPreferences.edit().putString(KEY_CORREO, correo).apply()
    }

    // --- MÉTODOS PARA RECUPERAR DATOS ---

    fun fetchAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun fetchUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    fun fetchUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun fetchUserEmail(): String? {
        return sharedPreferences.getString(KEY_CORREO, null)
    }

    fun getUser(): Usuario? {
        val usuarioJson = sharedPreferences.getString(KEY_JSON, null)
        if (usuarioJson.isNullOrEmpty()) return null

        return try {
            // ¡CORRECCIÓN DEFINITIVA!
            // Usamos la instancia de Gson que SÍ conoce nuestro TypeAdapter.
            GsonProvider.instance.fromJson(usuarioJson, Usuario::class.java)
        } catch (e: Exception) {
            // Si el JSON guardado está corrupto, lo mejor es limpiar la sesión.
            logout()
            null
        }
    }

    // --- CERRAR SESIÓN ---
    fun logout() {
        // .clear() borra todas las preferencias de una vez
        sharedPreferences.edit().clear().apply()
    }
}

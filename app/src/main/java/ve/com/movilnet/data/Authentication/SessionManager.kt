package ve.com.movilnet.data.Authentication

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    // Claves para guardar los valores
    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_AUTH_TOKEN = "token"
        private const val KEY_USER_ROLE = "role"
    }

    /**
     * Guarda el token de autenticación en SharedPreferences.
     * @param token El token a guardar.
     */
    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply() // Guarda los cambios de forma asíncrona.
    }

    /**
     * Recupera el token de autenticación desde SharedPreferences.
     * @return El token guardado, o null si no existe.
     */
    fun fetchAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Guarda el rol del usuario.
     * @param role El rol a guardar (ej. "admin", "user").
     */
    fun saveUserRole(role: String) {
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }

    /**
     * Recupera el rol del usuario.
     * @return El rol guardado, o null si no existe.
     */
    fun fetchUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    /**
     * Borra el token de autenticación para cerrar la sesión.
     */
    fun logout() {
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_USER_ROLE)
        editor.apply()
    }
}
/*
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extensión para crear una instancia de DataStore a nivel de aplicación (singleton)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MyAppPrefs")

/**
 * Clase moderna para gestionar la sesión del usuario usando Jetpack DataStore.
 * Es completamente asíncrona y segura para el hilo principal.
 *
 * @param context El contexto de la aplicación.
 */
class SessionManager(private val context: Context) {

    // Define una clave para guardar el token. Es como el nombre de la columna en una base de datos.
    private object PreferencesKeys {
        val USER_TOKEN = stringPreferencesKey("token")
    }

    /**
     * Guarda el token de autenticación de forma asíncrona.
     * Esta función es 'suspend', por lo que debe ser llamada desde una Coroutine.
     */
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_TOKEN] = token
        }
    }

    /**
     * Recupera el token de autenticación como un Flow.
     * Un Flow emite valores cada vez que el token cambia, permitiendo que tu UI reaccione automáticamente.
     */
    val authTokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_TOKEN]
        }

    /**
     * Recupera el token actual una sola vez.
     * Ideal para usar en interceptors de red donde solo necesitas el valor instantáneo.
     * Esta función es 'suspend'.
     */
    suspend fun fetchCurrentAuthToken(): String? {
        // .first() toma el primer valor emitido por el Flow y luego cancela la colección.
        return authTokenFlow.first()
    }
}*/
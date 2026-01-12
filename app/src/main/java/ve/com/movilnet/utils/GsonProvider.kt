package ve.com.movilnet.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ve.com.savam.data.models.Roles
import ve.com.savam.data.models.RolesTypeAdapter

/**
 * Objeto Singleton que proporciona una única instancia configurada de Gson
 * para toda la aplicación. Esto asegura que la (de)serialización sea consistente
 * en todas partes (Retrofit, SharedPreferences, etc.).
 */
object GsonProvider {

    // Crea una única instancia "lazy" (solo cuando se necesita por primera vez).
    val instance: Gson by lazy {
        GsonBuilder()
            // Aquí registramos nuestro TypeAdapter para la clase Roles.
            .registerTypeAdapter(Roles::class.java, RolesTypeAdapter())
            .create()
    }
}
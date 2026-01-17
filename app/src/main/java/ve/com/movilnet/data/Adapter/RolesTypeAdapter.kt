package ve.com.savam.data.models // Asegúrate de que el paquete sea el correcto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ve.com.movilnet.data.Response.RolesResponse

/**
 * Este TypeAdapter maneja el caso donde el campo 'roles'
 * a veces es un objeto JSON y otras veces es un simple String (ID).
 */
class RolesTypeAdapter : TypeAdapter<RolesResponse>() {

    override fun write(out: JsonWriter, value: RolesResponse?) {
        // Lógica para escribir (convertir de objeto a JSON), si es necesaria.
        // Por ahora, lo dejamos simple.
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("id").value(value.id)
        out.name("nombre").value(value.nombre)
        out.name("descripcion").value(value.descripcion)
        out.endObject()
    }

    override fun read(reader: JsonReader): RolesResponse? {
        // peek() nos deja ver el tipo del siguiente token sin consumirlo.
        when (reader.peek()) {

            // Si el valor en el JSON es `null`
            JsonToken.NULL -> {
                reader.nextNull() // Consumimos el null
                return null
            }

            // CASO 1: Cuando la API devuelve solo el ID como un String.
            // Ejemplo JSON: "roles": "a1b2c3d4-e5f6-..."
            JsonToken.STRING -> {
                val roleId = reader.nextString()
                // Creamos un objeto RolesResponse parcial, solo con el ID.
                return RolesResponse(
                    id = roleId,
                    nombre = null,
                    descripcion = null,
                    permisos = null
                )
            }

            // CASO 2: Cuando la API devuelve el objeto Rol completo.
            // Ejemplo JSON: "roles": { "id": "...", "nombre": "Moderador", "accion": ["crear"] }
            JsonToken.BEGIN_OBJECT -> {
                // Variables locales para ir guardando los valores que encontramos.
                var id: String? = null
                var nombre: String? = null
                var descripcion: String? = null
                val permisos = mutableListOf<String>() // Lista mutable para los permisos

                reader.beginObject() // Empezamos a leer el objeto
                while (reader.hasNext()) {
                    // --- ¡CORRECCIÓN APLICADA AQUÍ! ---
                    when (reader.nextName()) {
                        // Si el campo del JSON es "id" o "_id", lo asignamos a nuestra variable `id`.
                        "id", "_id" -> id = reader.nextString()

                        // Si el campo del JSON es "nombre", lo asignamos a nuestra variable `nombre`.
                        "nombre" -> nombre = reader.nextString()

                        // Si el campo del JSON es "descripcion", lo asignamos a `descripcion`.
                        "descripcion" -> descripcion = reader.nextString()

                        // Si el campo del JSON es "accion", leemos el array de permisos.
                        "accion" -> {
                            // Es buena práctica verificar si de verdad es un array.
                            if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    permisos.add(reader.nextString())
                                }
                                reader.endArray()
                            } else {
                                // Si "accion" no es un array (ej. es null), lo ignoramos.
                                reader.skipValue()
                            }
                        }

                        // Si encontramos un campo que no nos interesa, lo saltamos.
                        else -> reader.skipValue()
                    }
                }
                reader.endObject() // Terminamos de leer el objeto

                // Creamos el objeto RolesResponse con los valores correctos que hemos recolectado.
                return RolesResponse(
                    id,
                    nombre,
                    descripcion,
                    if (permisos.isNotEmpty()) permisos else null
                )
            }

            // Si el token es de un tipo inesperado (ni null, ni String, ni Objeto), lo ignoramos.
            else -> {
                reader.skipValue()
                return null
            }
        }
    }
}

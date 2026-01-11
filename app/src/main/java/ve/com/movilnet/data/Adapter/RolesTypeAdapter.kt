package ve.com.savam.data.models // Asegúrate de que el paquete sea el correcto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Este TypeAdapter maneja el caso donde el campo 'roles'
 * a veces es un objeto JSON y otras veces es un simple String (ID).
 */
class RolesTypeAdapter : TypeAdapter<Roles>() {

    override fun write(out: JsonWriter, value: Roles?) {
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

    override fun read(reader: JsonReader): Roles? {
        // --- ¡AQUÍ ESTÁ LA MAGIA! ---
        // Lógica para leer (convertir de JSON a objeto).

        // Si el token es null, devolvemos null
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        // Si el token es un STRING (como "6949b74d15750b560f9dc29d")
        if (reader.peek() == JsonToken.STRING) {
            val roleId = reader.nextString()
            // Creamos un objeto Roles solo con el ID, el resto es nulo.
            return Roles(id = roleId, nombre = null, descripcion = null, permisos = null)
        }

        // Si el token es un OBJETO (como {"id": "...", "nombre": "..."})
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            var id: String? = null
            var nombre: String? = null
            var descripcion: String? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id", "_id" -> id = reader.nextString() // Aceptamos 'id' o '_id'
                    "nombre" -> nombre = reader.nextString()
                    "descripcion" -> descripcion = reader.nextString()
                    else -> reader.skipValue() // Ignoramos otros campos
                }
            }
            reader.endObject()
            return Roles(id = id, nombre = nombre, descripcion = descripcion, permisos = null)
        }

        // Si es algo inesperado, lo ignoramos y devolvemos null
        reader.skipValue()
        return null
    }
}

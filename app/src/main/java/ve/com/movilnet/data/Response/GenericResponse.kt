package ve.com.movilnet.data.Response

// En un nuevo archivo llamado GenericResponse.kt (puedes reutilizarlo)
data class GenericResponse(
    val success: Boolean,
    val message: String,
    val token: String? // El token puede ser opcional
)

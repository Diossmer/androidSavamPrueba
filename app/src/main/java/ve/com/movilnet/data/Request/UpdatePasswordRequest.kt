package ve.com.movilnet.data.Request

// En un nuevo archivo llamado UpdatePasswordRequest.kt
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

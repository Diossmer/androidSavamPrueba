package ve.com.movilnet.data.Model

// En un nuevo archivo llamado UpdatePasswordRequest.kt
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

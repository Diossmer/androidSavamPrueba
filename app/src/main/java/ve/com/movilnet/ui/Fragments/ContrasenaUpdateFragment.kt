package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import ve.com.movilnet.R
import ve.com.movilnet.data.Authentication.SessionManager
import ve.com.movilnet.data.Model.UpdatePasswordRequest
import ve.com.movilnet.utils.RetrofitClient


/**
 * A simple [Fragment] subclass.
 * Use the [ContrasenaUpdateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContrasenaUpdateFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var currentPasswordLayout: TextInputLayout
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var editTextCurrentPassword: TextInputEditText
    private lateinit var editTextNewPassword: TextInputEditText
    private lateinit var buttonUpdatePassword: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var authTokenManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        currentPasswordLayout = view.findViewById(R.id.currentPasswordLayout)
        newPasswordLayout = view.findViewById(R.id.newPasswordLayout)
        editTextCurrentPassword = view.findViewById(R.id.editTextCurrentPassword)
        editTextNewPassword = view.findViewById(R.id.editTextNewPassword)
        buttonUpdatePassword = view.findViewById(R.id.buttonUpdatePassword)
        progressBar = view.findViewById(R.id.progressBar)

        // Inicializar el gestor de token
        authTokenManager = SessionManager(requireContext())

        buttonUpdatePassword.setOnClickListener {
            handleUpdatePassword()
        }
    }
    private fun handleUpdatePassword() {
        val currentPassword = editTextCurrentPassword.text.toString()
        val newPassword = editTextNewPassword.text.toString()

        if (validateInput(currentPassword, newPassword)) {
            val token = authTokenManager.fetchAuthToken()
            if (token == null) {
                Toast.makeText(context, "Error: Sesión no encontrada. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
                // Opcional: Redirigir al login
                return
            }

            // El formato del token debe ser "Bearer <token>"
            val formattedToken = "Bearer $token"
            performApiCall(formattedToken, currentPassword, newPassword)
        }
    }

    private fun validateInput(current: String, new: String): Boolean {
        // Limpiar errores previos
        currentPasswordLayout.error = null
        newPasswordLayout.error = null

        if (current.isEmpty()) {
            currentPasswordLayout.error = "La contraseña actual es requerida."
            return false
        }

        if (new.length < 8) {
            newPasswordLayout.error = "La nueva contraseña debe tener al menos 8 caracteres."
            return false
        }

        if (new == current) {
            newPasswordLayout.error = "La nueva contraseña no puede ser igual a la actual."
            return false
        }

        return true
    }

    private fun performApiCall(token: String, current: String, new: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            setLoading(true)
            try {
                val request = UpdatePasswordRequest(currentPassword = current, newPassword = new)
                // Asume que tienes un objeto RetrofitClient como en el ejemplo anterior
                val response = RetrofitClient.updatePasswodServices.updatePassword(token, request)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Toast.makeText(context, responseBody?.message ?: "¡Contraseña actualizada con éxito!", Toast.LENGTH_SHORT).show()
                    // Opcional: Navegar a otra pantalla o limpiar los campos
                    editTextCurrentPassword.text?.clear()
                    editTextNewPassword.text?.clear()
                } else {
                    // Manejar errores de la API (ej: contraseña actual incorrecta)
                    val errorMsg = "Error ${response.code()}: Contraseña actual incorrecta o error del servidor."
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Manejar errores de red
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            buttonUpdatePassword.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            buttonUpdatePassword.isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contrasena_update, container, false)
    }
}
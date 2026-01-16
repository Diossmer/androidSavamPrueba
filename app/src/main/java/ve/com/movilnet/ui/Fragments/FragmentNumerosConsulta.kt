// Define que este archivo pertenece al "paquete" o carpeta "ui.Fragments"
package ve.com.movilnet.ui.Fragments

// Importa herramientas necesarias de Android
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.launch
import androidx.fragment.app.Fragment // La herramienta para crear "pegatinas"
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ve.com.movilnet.R // El acceso a todos tus recursos (layouts, imágenes, etc.)
import ve.com.movilnet.data.Model.ConsultaRequest
import ve.com.movilnet.utils.RetrofitClient

// 1. La Declaración de la Clase
class FragmentNumerosConsulta: Fragment() {

    private lateinit var editTextNumero: EditText
    private lateinit var buttonConsultar: Button
    private lateinit var textViewResultado: TextView
    private lateinit var progressBar: ProgressBar

    // 2. El Método que "Dibuja" el Fragmento
    override fun onCreateView(
        inflater: LayoutInflater, // La "Impresora 3D"
        container: ViewGroup?,    // El "Espacio Asignado" en la página
        savedInstanceState: Bundle? // La "Memoria de Estado"
    ): View? { // <- Tiene que devolver el resultado visual

        // 3. La Acción de Inflar (Crear la Vista)
        return inflater.inflate(R.layout.fragment_numero_consulta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar las vistas
        editTextNumero = view.findViewById(R.id.editTextNumero)
        buttonConsultar = view.findViewById(R.id.buttonConsultar)
        textViewResultado = view.findViewById(R.id.textViewResultado)
        progressBar = view.findViewById(R.id.progressBar)

        // Configurar el listener del botón
        buttonConsultar.setOnClickListener {
            val numero = editTextNumero.text.toString().trim()
            if (esNumeroValido(numero)) {
                realizarConsulta(numero)
            }
        }
    }

    private fun esNumeroValido(numero: String): Boolean {
        // 1. Validar que no esté vacío
        if (numero.isEmpty()) {
            editTextNumero.error = "El número no puede estar vacío."
            return false
        }

        // 2. Validar longitud
        if (numero.length != 11) {
            editTextNumero.error = "El número debe tener 11 dígitos."
            return false
        }

        // 3. Validar que comience con 0416 o 0426
        if (!numero.startsWith("0416") && !numero.startsWith("0426")) {
            editTextNumero.error = "El número debe comenzar con 0416 o 0426."
            return false
        }

        // Si pasa todas las validaciones
        editTextNumero.error = null // Limpiar errores previos
        return true
    }

    private fun realizarConsulta(numero: String) {
        // Usamos una corrutina para la llamada de red
        viewLifecycleOwner.lifecycleScope.launch {
            // Mostrar ProgressBar y ocultar resultado anterior
            progressBar.visibility = View.VISIBLE
            textViewResultado.text = ""
            buttonConsultar.isEnabled = false

            try {
                val request = ConsultaRequest(numero = numero)
                val response = RetrofitClient.numeroConsultaServices.consultarNumero(request)

                if (response.isSuccessful) {
                    // La llamada fue exitosa (código 2xx)
                    val consultaResponse = response.body()

                    // Verificamos que la respuesta, el objeto 'data' y el objeto 'whatsapp' no sean nulos
                    if (consultaResponse != null && consultaResponse.data != null) {

                        // La clave está aquí: accedemos a través de .data.whatsapp
                        // --- SECCIÓN WHATSAPP ---
                        val whatsappInfo = consultaResponse.data.whatsapp
                        val tituloWhatsApp = whatsappInfo.titulo_pagina // Usamos un nombre más específico
                        val estadoWhatsApp = if (whatsappInfo.tiene_whatsapp) {
                            "Sí posee WhatsApp."
                        } else {
                            "No posee WhatsApp."
                        }

                        // --- SECCIÓN TELEGRAM (LA NUEVA PARTE) ---
                        val telegramInfo = consultaResponse.data.telegram
                        val estadoTelegram = if (telegramInfo.tiene_telegram) {
                            "Sí posee Telegram."
                        } else {
                            // Usamos el mensaje que nos da la API para Telegram
                            telegramInfo.mensaje
                        }

                        // --- CONSTRUCCIÓN DEL TEXTO FINAL ---
                        // Unimos toda la información en un solo bloque de texto para el TextView.
                        // Usamos \n para los saltos de línea.
                        textViewResultado.text = """
                            WhatsApp:
                            Título: $tituloWhatsApp
                            Estado: $estadoWhatsApp
                    
                            Telegram:
                            Estado: $estadoTelegram
                        """.trimIndent() // trimIndent() quita los espacios en blanco del inicio para que se vea bien

                    } else {
                        // Si por alguna razón la respuesta no tiene el formato esperado (es nula o no tiene 'data')
                        val errorMessage = consultaResponse?.message ?: "Respuesta con formato inesperado."
                        textViewResultado.text = "Info: $errorMessage"
                    }

                } else {
                    // La llamada falló (código 4xx o 5xx)
                    textViewResultado.text = "Error: ${response.code()} - ${response.message()}"
                }

            } catch (e: Exception) {
                // Error de conexión, timeout, etc.
                textViewResultado.text = "Error de conexión: ${e.message}"
                Toast.makeText(context, "No se pudo conectar al servidor.", Toast.LENGTH_LONG).show()

            } finally {
                // Ocultar ProgressBar y reactivar el botón
                progressBar.visibility = View.GONE
                buttonConsultar.isEnabled = true
            }
        }
    }
}

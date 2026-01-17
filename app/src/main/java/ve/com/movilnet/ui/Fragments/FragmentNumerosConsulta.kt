package ve.com.movilnet.ui.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ve.com.movilnet.R
import ve.com.movilnet.data.Authentication.SessionManager
import ve.com.movilnet.data.Request.ConsultaRequest
import ve.com.movilnet.data.Request.NumeroApiRequest
import ve.com.movilnet.utils.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentNumerosConsulta : Fragment() {

    private lateinit var editTextNumero: EditText
    private lateinit var buttonConsultar: Button
    private lateinit var textViewResultado: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_numero_consulta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextNumero = view.findViewById(R.id.editTextNumero)
        buttonConsultar = view.findViewById(R.id.buttonConsultar)
        textViewResultado = view.findViewById(R.id.textViewResultado)
        progressBar = view.findViewById(R.id.progressBar)

        buttonConsultar.setOnClickListener {
            val numero = editTextNumero.text.toString().trim()
            if (esNumeroValido(numero)) {
                // El botón solo llama a la función orquestadora
                consultarYRegistrar(numero)
            }
        }
    }

    private fun esNumeroValido(numero: String): Boolean {
        if (numero.isEmpty()) {
            editTextNumero.error = "El número no puede estar vacío."
            return false
        }
        if (numero.length != 11) {
            editTextNumero.error = "El número debe tener 11 dígitos."
            return false
        }
        if (!numero.startsWith("0416") && !numero.startsWith("0426")) {
            editTextNumero.error = "El número debe comenzar con 0416 o 0426."
            return false
        }
        editTextNumero.error = null
        return true
    }

    // --- FUNCIÓN ORQUESTADORA PRINCIPAL ---
    @SuppressLint("SetTextI18n")
    private fun consultarYRegistrar(numero: String) {
        // 1. Lanzamos UNA SOLA corrutina para controlar toda la operación
        viewLifecycleOwner.lifecycleScope.launch {
            // 2. Preparamos la UI para la operación
            progressBar.visibility = View.VISIBLE
            textViewResultado.text = ""
            buttonConsultar.isEnabled = false

            // 3. Lanzamos la tarea de GUARDADO en segundo plano.
            //    No esperamos a que termine, se ejecuta en paralelo.
            launch {
                guardarRegistro(numero)
            }

            // 4. Realizamos la CONSULTA principal y actualizamos la UI con su resultado.
            try {
                val request = NumeroApiRequest(numero = numero)
                val response = RetrofitClient.numeroConsultaServices.consultarNumero(request)

                if (response.isSuccessful) {
                    val consultaResponse = response.body()
                    if (consultaResponse != null && consultaResponse.data != null) {
                        val estado = consultaResponse.data.status
                        val whatsappInfo = consultaResponse.data.whatsapp
                        val telegramInfo = consultaResponse.data.telegram

                        val tituloWhatsApp = whatsappInfo.titulo_pagina
                        val estadoWhatsApp = if (whatsappInfo.tiene_whatsapp) "Sí posee WhatsApp." else "No posee WhatsApp."
                        val estadoTelegram = if (telegramInfo.tiene_telegram) "Sí posee Telegram." else telegramInfo.mensaje

                        // Actualizamos el TextView con los resultados
                        textViewResultado.text = """
                            WhatsApp:
                            Título: $tituloWhatsApp
                            Estado: $estadoWhatsApp

                            Telegram:
                            Estado: $estadoTelegram
                        """.trimIndent()

                        // Si falta algún servicio, mostramos el diálogo
                        if (!whatsappInfo.tiene_whatsapp || !telegramInfo.tiene_telegram) {
                            mostrarDialogoAsignarDatos(estado, numero, !whatsappInfo.tiene_whatsapp, !telegramInfo.tiene_telegram)
                        }
                    } else {
                        textViewResultado.text = "Info: ${consultaResponse?.message ?: "Respuesta con formato inesperado."}"
                    }
                } else {
                    textViewResultado.text = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                textViewResultado.text = "Error de conexión: ${e.message}"
                Toast.makeText(context, "No se pudo conectar al servidor.", Toast.LENGTH_LONG).show()
            } finally {
                // 5. Al final de la CONSULTA (éxito o error), restauramos la UI.
                progressBar.visibility = View.GONE
                buttonConsultar.isEnabled = true
            }
        }
    }

    // --- Función dedicada únicamente a GUARDAR el registro ---
    private suspend fun guardarRegistro(numero: String) {
        val safeContext = context ?: return
        try {
            val sessionManager = SessionManager.getInstance(safeContext)
            val usuarioLogueado = sessionManager.getUser()

            // --- VALIDACIÓN CLAVE ---
            // 1. Verificamos que el objeto de usuario no sea nulo
            if (usuarioLogueado == null) {
                // Usamos Toast en el hilo principal si es necesario.
                // Para eso, envolvemos la lógica de la UI.
                withContext(Dispatchers.Main) {
                    Toast.makeText(safeContext, "Aviso: Sesión no encontrada. No se registró.", Toast.LENGTH_SHORT).show()
                }
                return // Detenemos la función aquí si no hay usuario
            }

            // 2. Verificamos que la cédula dentro del usuario no sea nula ni esté vacía
            val cedulaUsuario = usuarioLogueado.cedula // Asumiendo que el campo se llama 'cedula'
            if (cedulaUsuario.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(safeContext, "Aviso: Cédula de usuario no encontrada. No se registró.", Toast.LENGTH_SHORT).show()
                }
                return // Detenemos la función si la cédula es inválida
            }

            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val request = ConsultaRequest(cedula = cedulaUsuario, numero = numero, fechaDeConsulta = fechaActual)

            // Llamamos a la API para guardar. Renombré la función en la interfaz para mayor claridad.
            RetrofitClient.numeroConsultaServices.guardarResultadoConsulta(request)
            // Si llega aquí, se guardó (o al menos se envió) correctamente.
        } catch (e: Exception) {
            // Si falla el guardado, mostramos un aviso discreto. La app sigue funcionando.
            // Es importante que la corrutina que llama a esto pueda manejar el error sin detenerse.
            Toast.makeText(context, "Aviso: No se pudo registrar la consulta.${e.message.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Función dedicada únicamente a MOSTRAR el diálogo ---
    private fun mostrarDialogoAsignarDatos(
        estado: String?,
        numero: String,
        faltaWhatsApp: Boolean,
        faltaTelegram: Boolean
    ) {
        val safeContext = context ?: return
        val motivo = when {
            faltaWhatsApp && faltaTelegram -> "WhatsApp y Telegram"
            faltaWhatsApp -> "WhatsApp"
            faltaTelegram -> "Telegram"
            else -> ""
        }

        if (motivo.isNotEmpty()) {
            val mensajeCompleto = "El suscriptor no tiene registro para $motivo. ¿Deseas asignarle el $numero?"
            MaterialAlertDialogBuilder(safeContext)
                .setTitle("Acción Sugerida")
                .setMessage(mensajeCompleto)
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Aceptar") { _, _ ->
                    // --- ¡AQUÍ ESTÁ EL CAMBIO CLAVE! ---

                    // 1. Creamos una instancia de nuestro nuevo BottomSheet
                    //    usando el método `newInstance` para pasar los datos de forma segura.
                    //    - El número ya lo tenemos.
                    //    - Si 'faltaWhatsApp' es true, significa que no tiene, así que el switch empieza en 'false'.
                    val bottomSheet = SuscriptorBottomSheetFragment.newInstance(
                        numero = numero,
                        tieneWhatsapp = !faltaWhatsApp, // Invertimos la lógica
                        tieneTelegram = !faltaTelegram,
                        estatus = estado
                    )

                    // 2. Mostramos el BottomSheet.
                    //    Usamos 'childFragmentManager' porque estamos lanzando un fragmento desde otro fragmento.
                    bottomSheet.show(childFragmentManager, SuscriptorBottomSheetFragment.TAG)
                    Toast.makeText(safeContext, "Datos del Suscriptor.", Toast.LENGTH_LONG).show()
                }
                .show() // <-- Un solo show() al final es correcto
        }
    }
}

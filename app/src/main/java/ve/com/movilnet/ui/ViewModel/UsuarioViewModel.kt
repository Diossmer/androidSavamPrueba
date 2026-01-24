package ve.com.movilnet.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ve.com.movilnet.data.Request.UsuarioRequest
import ve.com.movilnet.utils.RetrofitClient
import ve.com.movilnet.data.Response.RolesResponse
import ve.com.movilnet.data.Response.UsuarioResponse
import kotlin.text.lowercase

class UsuarioViewModel : ViewModel() {
    // LiveData privado y mutable, solo el ViewModel puede cambiar su valor.
    private val _usuarios = MutableLiveData<List<UsuarioResponse>>()
    // LiveData público e inmutable, la vista solo puede observar sus cambios.
    val usuarios: LiveData<List<UsuarioResponse>> = _usuarios
    // LiveData para manejar errores o estados de carga.
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    // --- NUEVO LIVE DATA PARA ERRORES DE VALIDACIÓN ---
    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _roles = MutableLiveData<List<RolesResponse>>()
    val roles: LiveData<List<RolesResponse>> = _roles
    // --- NUEVO LIVE DATA PARA EL USUARIO SELECCIONADO ---
    private val _usuarioSeleccionado = MutableLiveData<UsuarioResponse?>()
    val usuarioSeleccionado: LiveData<UsuarioResponse?> = _usuarioSeleccionado
    // --- LiveData para el usuario que ha iniciado sesión ---
    private val _loggedInUser = MutableLiveData<UsuarioResponse?>()
    val loggedInUser: LiveData<UsuarioResponse?> = _loggedInUser
    // --- Lista para guardar la fuente de datos original ---
    private var listaCompletaDeUsuarios: List<UsuarioResponse> = emptyList()


    // --- 1. FUNCIÓN PÚBLICA PARA CARGAR DATOS ---
    /**
     * Esta es la función que llamas desde el Fragment.
     * Carga la lista completa de la API y luego aplica los filtros.
     */
    fun cargarListaDeUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.usuariosServices.listUsuarios()
                if (response.isSuccessful) {
                    // Guarda la lista completa en nuestra variable privada. ¡Esta es la fuente de verdad!
                    listaCompletaDeUsuarios = response.body() ?: emptyList()
                    // Ahora, aplica los filtros iniciales (rol, etc.) sobre esa lista.
                    aplicarFiltros()
                } else {
                    _errorMessage.value = "Error al obtener usuarios: ${response.code()}"
                    _usuarios.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo en la conexión: ${e.message}"
                _usuarios.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    // --- 2. FUNCIÓN PÚBLICA PARA BUSCAR ---
    /**
     * Filtra la lista que ya está en memoria. No llama a la API.
     */
    fun buscarUsuario(query: String) {
        aplicarFiltros(query)
    }

    // --- 3. LÓGICA CENTRAL DE FILTRADO (PRIVADA) ---
    /**
     * Aplica TODOS los filtros (rol y búsqueda) a la `listaCompletaDeUsuarios`.
     */
    private fun aplicarFiltros(queryDeBusqueda: String = "") {
        // Empezamos siempre desde la lista completa y sin tocar
        var listaFiltrada = listaCompletaDeUsuarios
        val usuarioActual = _loggedInUser.value

        // Filtro 1: Por Rol (la lógica que ya tenías en fetchUsuarios)
        if (!esUsuarioAdministrador(usuarioActual)) {
            // Si no es admin, quitamos a los administradores de la vista.
            listaFiltrada = listaFiltrada.filter { usuario -> esUsuarioVisibleEnLista(usuario) }
        }

        // Filtro 2: Por Término de Búsqueda
        if (queryDeBusqueda.isNotBlank()) {
            val queryLower = queryDeBusqueda.lowercase()
            listaFiltrada = listaFiltrada.filter { usuario ->
                (usuario.nombre?.lowercase()?.contains(queryLower) == true) ||
                        (usuario.apellido?.lowercase()?.contains(queryLower) == true) ||
                        (usuario.cedula?.lowercase()?.contains(queryLower) == true) ||
                        (usuario.correo?.lowercase()?.contains(queryLower) == true) ||
                        (usuario.roles?.nombre?.lowercase()?.contains(queryLower) == true)
            }
        }

        // Al final, publicamos el resultado en el LiveData que el Fragment observa.
        _usuarios.postValue(listaFiltrada)
    }

    /**
     * Limpia el LiveData del usuario seleccionado.
     * Es crucial llamar a esta función antes de abrir el formulario en modo "Agregar"
     * o al cerrar el formulario para evitar que datos antiguos se muestren.
     */
    fun limpiarUsuarioSeleccionado() {
        _usuarioSeleccionado.value = null
    }

    /**
     * Limpia el mensaje de error de validación.
     * Debes llamar a esta función desde la vista (Fragment/Activity)
     * después de haber mostrado el mensaje de error al usuario.
     */
    fun limpiarErrorDeValidacion() {
        _validationError.value = null
    }

    /**
     * Establece el usuario que ha iniciado sesión.
     * Deberías llamar a esta función después de un login exitoso.
     */
    fun setLoggedInUser(usuario: UsuarioResponse?) {
        _loggedInUser.value = usuario
    }

    /**
     * Comprueba si un usuario dado tiene el rol de "Administrador".
     * Esta función es privada para asegurar que la lógica de validación de roles
     * se mantenga encapsulada dentro del ViewModel.
     *
     * @param usuario El objeto UsuarioResponse a verificar.
     * @return `true` si el usuario no es nulo y su rol es "Administrador", `false` en caso contrario.
     */
    private fun esUsuarioAdministrador(usuario: UsuarioResponse?): Boolean {
        // Si el usuario que nos pasan es nulo, no puede ser admin.
        if (usuario == null) {
            Log.d("AuthCheck", "Chequeo de rol fallido: el objeto usuario es nulo.")
            return false
        }

        // Comparamos el nombre del rol de forma segura, ignorando mayúsculas/minúsculas.
        val esAdmin = usuario.roles?.nombre?.equals("Administrador", ignoreCase = true) == true

        Log.d(
            "AuthCheck",
            "Chequeando rol para: ${usuario.nombre}, Rol: '${usuario.roles?.nombre}', ¿Es Admin? -> $esAdmin"
        )
        return esAdmin
    }

    /**
     * Comprueba si un usuario debe ser visible en la lista general (es decir, no es ni Admin ni Moderador).
     * @param usuario El objeto UsuarioResponse a verificar.
     * @return `true` si el usuario no es Administrador ni Moderador, `false` en caso contrario.
     */
    private fun esUsuarioVisibleEnLista(usuario: UsuarioResponse?): Boolean {
        if (usuario?.roles?.nombre == null) {
            // Si el usuario o su rol son nulos, lo consideramos no visible para evitar errores.
            return false
        }
        val rolNombre = usuario.roles.nombre
        // La condición es: el rol NO es "Administrador" Y TAMPOCO es "Moderador".
        val noEsAdmin = !rolNombre.equals("Administrador", ignoreCase = true)
        val noEsModerador = !rolNombre.equals("Moderador", ignoreCase = true)

        return noEsAdmin && noEsModerador
    }

    /**
     * Obtiene la lista de usuarios según el rol del usuario que ha iniciado sesión.
     * - Si es Administrador, obtiene la lista completa desde la API.
     * - Si no es Administrador, solo muestra el perfil del propio usuario.
     */
    /*fun fetchUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true

            val usuarioActual = _loggedInUser.value

            // --- VALIDACIÓN DE ROL DE ADMINISTRADOR ---
            // Se pasa el usuario actual a la función de validación.
            if (esUsuarioAdministrador(usuarioActual)) {
                // CASO 1: Es Administrador -> Visualiza la lista completa.
                Log.d("fetchUsuarios", "UsuarioResponse es Administrador. Obteniendo lista completa de la API.")
                try {
                    val response = RetrofitClient.usuariosServices.listUsuarios()
                    if (response.isSuccessful) {
                        // La vista que observa 'usuarios' se actualizará para mostrar a todos.
                        _usuarios.value = response.body()
                    } else {
                        _errorMessage.value = "Error al obtener usuarios: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    // Captura excepciones como falta de conexión a internet.
                    _errorMessage.value = "Fallo en la conexión: ${e.message}"
                }
            } else {
                // CASO 2: NO es Administrador (ej: Moderador, Agente, etc.)
                Log.d("fetchUsuarios", "UsuarioResponse NO es Administrador. Obteniendo lista y filtrando admins.")
                try {
                    // 1. Llama a la API para obtener la lista completa de usuarios.
                    val response = RetrofitClient.usuariosServices.listUsuarios()
                    if (response.isSuccessful) {
                        // 2. Filtra la lista para excluir a los administradores.
                        //    Usamos la misma función `esUsuarioAdministrador` que ya tienes.
                        val listaFiltrada = response.body()?.filter { usuario ->
                            !esUsuarioAdministrador(usuario)
                        }

                        // 3. Asigna la lista ya filtrada al LiveData.
                        _usuarios.value = listaFiltrada?: emptyList()
                    } else {
                        // Manejo de error si la llamada a la API falla.
                        _errorMessage.value = "Error al obtener usuarios: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    // Manejo de error si hay un problema de conexión.
                    _errorMessage.value = "Fallo en la conexión: ${e.message}"
                }
            }

            // Indica que la operación de carga (exitosa o no) ha finalizado.
            _isLoading.value = false
        }
    }*/

    fun fetchRoles() {
        viewModelScope.launch {
            _isLoading.value = true // Opcional: puedes manejar un estado de carga para los roles

            try {
                // 1. Obtener la lista COMPLETA de roles desde la API
                val response = RetrofitClient.rolesServices.listRoles()

                if (response.isSuccessful) {
                    val todosLosRoles = response.body()

                    // 2. Obtener el usuario que ha iniciado sesión
                    val usuarioActual = _loggedInUser.value

                    // 3. Comprobar si el usuario actual NO es administrador
                    if (!esUsuarioAdministrador(usuarioActual)) {
                        // Si NO es Admin (ej: Moderador, Agente), filtramos la lista
                        Log.d("fetchRoles", "UsuarioResponse no es Administrador. Filtrando la lista de roles.")
                        val rolesFiltrados = todosLosRoles?.filter { rol ->
                            // Comparamos el nombre del rol, ignorando mayúsculas/minúsculas
                            !rol.nombre.equals("Administrador", ignoreCase = true) &&
                                    !rol.nombre.equals("Moderador", ignoreCase = true) // Tambien aqui prohibo el moderador en la lista de usuario
                        }
                        // Asignamos la lista filtrada (sin el rol "Administrador")
                        _roles.value = rolesFiltrados ?: emptyList()
                    } else {
                        // Si ES Admin (o no hay usuario logueado, lo cual es un caso borde),
                        // mostramos la lista completa de roles.
                        Log.d("fetchRoles", "UsuarioResponse es Administrador. Mostrando todos los roles.")
                        _roles.value = todosLosRoles?: emptyList()
                    }
                } else {
                    // Manejo de error si la llamada a la API falla
                    _errorMessage.value = "Error al obtener roles: ${response.code()}"
                }
            } catch (e: Exception) {
                // Manejo de error de conexión
                _errorMessage.value = "Fallo de conexión al obtener roles: ${e.message}"
            } finally {
                _isLoading.value = false // Finaliza la carga
            }
        }
    }

    // --- NUEVA FUNCIÓN DE VALIDACIÓN CENTRALIZADA (CORREGIDA) ---
    private fun validarCamposUsuario(usuario: UsuarioResponse, esNuevoUsuario: Boolean): Boolean {
        // Validación 1: Campos básicos no deben estar vacíos
        if (usuario.nombre.isNullOrBlank() || usuario.apellido.isNullOrBlank() || usuario.cedula.isNullOrBlank() || usuario.correo.isNullOrBlank()) {
            _validationError.value = "Todos los campos son obligatorios."
            return false
        }

        // --- VALIDACIÓN DE NOMBRE Y APELLIDO (YA ES CORRECTA) ---
        // Esta expresión regular permite letras (incluyendo acentos) y espacios.
        val soloLetrasRegex = Regex("^[\\p{L} ]+\$")

        if (!soloLetrasRegex.matches(usuario.nombre)) {
            _validationError.value = "El nombre solo debe contener letras y espacios."
            return false
        }

        if (!soloLetrasRegex.matches(usuario.apellido)) {
            _validationError.value = "El apellido solo debe contener letras y espacios."
            return false
        }
        // --- FIN DE LA VALIDACIÓN ---

        // Validación 3: Formato de la cédula (Corregido)
        // Permite cédulas de hasta 8 dígitos y asegura que solo contengan números.
        if (usuario.cedula.length > 8 || !usuario.cedula.all { it.isDigit() }) {
            _validationError.value = "La cédula debe contener hasta 8 dígitos numéricos."
            return false
        }

        // Validación 4: Contraseña (solo para usuarios nuevos o si se está cambiando)
        if (esNuevoUsuario && usuario.password.isNullOrBlank()) {
            _validationError.value = "La contraseña es obligatoria para nuevos usuarios."
            return false
        }
        if (!usuario.password.isNullOrBlank() && usuario.password.length <= 8) {
            _validationError.value = "La contraseña debe tener más de 8 caracteres."
            return false
        }

        // Validación 5: Formato del correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(usuario.correo).matches()) {
            _validationError.value = "El formato del correo electrónico no es válido."
            return false
        }

        // Validación 6: Rol debe estar seleccionado
        if (usuario.roles?.id == null) {
            _validationError.value = "Debe seleccionar un rol para el usuario."
            return false
        }

        // Si todas las validaciones pasan
        _validationError.value = null // Limpiar cualquier error previo
        return true
    }

    // Dentro de la clase UsuarioViewModel, añade estas funciones:
    fun agregarUsuario(usuario: UsuarioResponse) {
        // --- 1. VALIDACIÓN ---
        if (!validarCamposUsuario(usuario, esNuevoUsuario = true)) {
            return // Detiene la ejecución si la validación falla
        }

        viewModelScope.launch {
            try {
                // LA SOLUCIÓN: Obtener el primer elemento de la lista y LUEGO su id.
                // Igual que en `actualizarUsuario`.
                val rolId = usuario.roles!!.id

                // Esta verificación ahora funcionará correctamente.
                if (rolId == null) {
                    _errorMessage.value = "Error: El rol seleccionado no es válido."
                    return@launch
                }

                // Creamos el objeto UsuarioRequest
                val requestRequest = UsuarioRequest(
                    id = null,
                    nombre = usuario.nombre,
                    apellido = usuario.apellido,
                    cedula = usuario.cedula,
                    correo = usuario.correo,
                    password = usuario.password,
                    oficina = usuario.oficina,
                    estado = usuario.estado,
                    // Usamos el `rolId` que obtuvimos de la lista.
                    roles = listOf(rolId)
                )

                // ¡OJO! La API al CREAR devuelve un 'UsuarioResponse' con "roles" como String.
                // Esto fallará la deserialización con Gson.
                // La solución ideal es pedir al backend que la respuesta al crear sea consistente
                // y devuelva el objeto rol completo.
                val response = RetrofitClient.usuariosServices.storeUsuario(requestRequest)

                if (response.isSuccessful) {
                    //fetchUsuarios() // Refresca la lista después de agregar
                    cargarListaDeUsuarios()
                } else {
                    _errorMessage.value = "Error al agregar: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al agregar: ${e.message}"
            }
        }
    }

    fun mostrarUsuario(usuarioId: String) {
        // Debes usar una corrutina para llamadas suspend
        viewModelScope.launch {
            _isLoading.value = true // Indica que estás cargando algo
            try {
                val response = RetrofitClient.usuariosServices.showUsuario(usuarioId)
                if (response.isSuccessful) {
                    // SOLUCIÓN: Asigna el usuario único al nuevo LiveData
                    _usuarioSeleccionado.value = response.body()
                } else {
                    _errorMessage.value =
                        "Error al mostrar: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al mostrar: ${e.message}"
            }
            _isLoading.value = false // Termina la carga
        }
    }

    fun actualizarUsuario(usuario: UsuarioResponse) {
        // --- 1. VALIDACIÓN ---
        // Para actualizar, la contraseña no es obligatoria (es opcional si se quiere cambiar)
        if (!validarCamposUsuario(usuario, esNuevoUsuario = false)) {
            return // Detiene la ejecución si la validación falla
        }

        viewModelScope.launch {
            try {
                // Obtén el primer rol de la lista. Si no hay, no se podrá actualizar.
                val rolId = usuario.roles!!.id
                if (usuario.id == null || rolId == null) {
                    _errorMessage.value = "Error: Faltan datos para actualizar (ID de usuario o rol)."
                    return@launch
                }

                // ----> TRANSFORMACIÓN CLAVE <----
                val usuarioRequest = UsuarioRequest(
                    id = usuario.id,
                    nombre = usuario.nombre,
                    apellido = usuario.apellido,
                    cedula = usuario.cedula,
                    correo = usuario.correo,
                    password = usuario.password, // El backend debe ignorar esto si es nulo
                    oficina = usuario.oficina,
                    estado = usuario.estado,
                    roles = listOf(rolId) // Solo el ID en una lista
                )

                val response = RetrofitClient.usuariosServices.updateUsuario(usuario.id, usuarioRequest)

                if (response.isSuccessful) {
                    //fetchUsuarios() // Refresca la lista después de actualizar
                    cargarListaDeUsuarios()
                } else {
                    _errorMessage.value = "Error al actualizar: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al actualizar: ${e.message}"
            }
        }
    }

    fun eliminarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.usuariosServices.deletePost(usuarioId)
                if (response.isSuccessful) {
                    //fetchUsuarios() // Refresca la lista después de eliminar
                    cargarListaDeUsuarios()
                } else {
                    _errorMessage.value = "Error al eliminar: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fallo al eliminar: ${e.message}"
            }
        }
    }
}

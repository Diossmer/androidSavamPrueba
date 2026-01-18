package ve.com.movilnet.ui.ViewModel

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import ve.com.movilnet.MainActivity
import ve.com.movilnet.R
import ve.com.movilnet.data.Authentication.SessionManager
import ve.com.movilnet.ui.Fragments.ContrasenaUpdateFragment
import ve.com.movilnet.ui.Fragments.FragmentNumerosConsulta
import ve.com.movilnet.ui.Fragments.FragmentUsuarios
import ve.com.movilnet.ui.Fragments.NumerosFragment
import ve.com.movilnet.ui.Fragments.SuscriptorFragment

class SecondActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var navigationView: NavigationView
    private lateinit var sessionManager: SessionManager

    // --- 1. AÑADE ESTAS DOS PROPIEDADES ---
    private var backPressedTime: Long = 0 // Almacena la última vez que se presionó "atrás"
    private lateinit var backToast: Toast  // Almacena el mensaje "toast" para poder cancelarlo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        backToast = Toast.makeText(this, "Presiona de nuevo para salir", Toast.LENGTH_SHORT)
        // --- INICIALIZA SESSION MANAGER ---
        sessionManager = SessionManager(applicationContext)
        // --- Configurar el Toolbar ---
        val toolbar: Toolbar = findViewById(R.id.toolbar)// Le dice a la actividad que este es su toolbar oficial
        setSupportActionBar(toolbar) // Le dice a la actividad que este es su toolbar oficial

        // --- Conectar el DrawerLayout y crear el botón de hamburguesa ---
        drawerLayout = findViewById(R.id.mainSecond) // Usa el ID correcto del XML
        navigationView = findViewById(R.id.nav_view) // Inicializa navigation

        // Mueve la lógica de la cabecera aquí.
        val headerView = navigationView.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.credentialId)
        val userEmailTextView: TextView = headerView.findViewById(R.id.credentialCorreo)

        val userName = sessionManager.fetchUserName()
        val userEmail = sessionManager.fetchUserEmail()

        userNameTextView.text = userName ?: "Nombre no disponible"
        userEmailTextView.text = userEmail ?: "Email no disponible"
        // --- Configurar el botón de hamburguesa ---
        // Esta línea crea el botón (☰) y gestiona la apertura/cierre del menú
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar, // Lo conecta al toolbar
            R.string.navigation_drawer_open, // Texto para accesibilidad (abrir)
            R.string.navigation_drawer_close // Texto para accesibilidad (cerrar)
        )
        // Añade el "listener" para que el toggle funcione
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState() // Sincroniza el estado del botón (muy importante)
        // Opcional: Muestra el botón de "hacia atrás" o hamburguesa en el toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (sessionManager.fetchUserRole() == "Administrador" || sessionManager.fetchUserRole() == "Moderador") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentUsuarios()).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                FragmentNumerosConsulta()
            ).commit()
        }
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_salir -> {
                    // Ya NO gestiona la sesión. Solo devuelve un resultado.
                    val resultIntent = Intent()
                    // Usamos la constante definida en MainActivity
                    setResult(MainActivity.RESULT_LOGOUT, resultIntent)
                    finish() // Cierra esta actividad
                    true // Indica que el evento fue manejado
                }
                // Aquí puedes manejar otros clics del menú si los tienes
                R.id.nav_usuario -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentUsuarios()).commit()
                    drawerLayout.closeDrawers() // Cierra el menú después de la selección
                    true
                }

                R.id.nav_consultar -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentNumerosConsulta()).commit()
                    drawerLayout.closeDrawers() // Cierra el menú después de la selección
                    true
                }

                R.id.nav_suscriptor -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SuscriptorFragment()).commit()
                    drawerLayout.closeDrawers()
                    true
                }

                R.id.nav_cambioContrasena -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ContrasenaUpdateFragment()).commit()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_numeros -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NumerosFragment()).commit()
                    drawerLayout.closeDrawers()
                    true
                }

                else -> false // El evento no fue manejado
            }
        }
        // CORRECCIÓN: Tu código original usaba setOnApplyWindowInsetsListener,
        // que no es el listener correcto para clics en los items.
        // El correcto es setNavigationItemSelectedListener.
        setupMenuForRole()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainSecond)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // --- ESTA ES LA NUEVA FORMA DE MANEJAR EL BOTÓN "ATRÁS" ---
        // 1. Obtenemos el despachador de la actividad
        val dispatcher = onBackPressedDispatcher

        // 2. Creamos un "callback" (un oyente) y lo añadimos al despachador
        dispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 3. Dentro de este método, ponemos EXACTAMENTE la misma lógica que tenías antes.

                // Primero, comprueba si el menú lateral está abierto.
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Si el menú no está abierto, ejecuta la lógica de doble toque.
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        backToast.cancel()
                        // IMPORTANTE: Para que el callback cierre la actividad,
                        // debemos deshabilitarlo y llamar de nuevo al dispatcher.
                        isEnabled = false
                        dispatcher.onBackPressed()
                    } else {
                        backToast.show()
                        backPressedTime = System.currentTimeMillis()
                    }
                }
            }
        })
    }

    private fun setupMenuForRole() {
        // Obtenemos el rol guardado
        val userRole = sessionManager.fetchUserRole()

        // Obtenemos el menú del NavigationView
        val menu = navigationView.menu

        // Obtenemos los ítems específicos que queremos mostrar/ocultar por su ID
        val adminItem = menu.findItem(R.id.nav_usuario)
        val userProfileItem = menu.findItem(R.id.nav_consultar)
        val suscriptorItem = menu.findItem(R.id.nav_suscriptor)
        val numerosItem = menu.findItem(R.id.nav_numeros)
        val contrasenaUpdate = menu.findItem(R.id.nav_cambioContrasena)
        // Lógica para mostrar/ocultar ítems
        when (userRole) {
            "Administrador" -> {
                adminItem?.isVisible = true
                contrasenaUpdate.isVisible = false
                userProfileItem?.isVisible = false
                suscriptorItem?.isVisible = false
                numerosItem?.isVisible = false
            }

            "Agente" -> {
                // El usuario normal no ve el panel de admin
                adminItem?.isVisible = false
                contrasenaUpdate.isVisible = true
                userProfileItem?.isVisible = true
                suscriptorItem?.isVisible = false
                numerosItem?.isVisible = false
            }

            "Moderador" -> {
                // El usuario normal no ve el panel de agente
                adminItem?.isVisible = true
                contrasenaUpdate.isVisible = true
                userProfileItem?.isVisible = false
                suscriptorItem?.isVisible = true
                numerosItem?.isVisible = true
            }

            else -> {
                // Rol desconocido o nulo, ocultar todo como medida de seguridad
                adminItem?.isVisible = false
                contrasenaUpdate.isVisible = false
                userProfileItem?.isVisible = false
                suscriptorItem?.isVisible = false
                numerosItem?.isVisible = false
                // Aquí podrías incluso cerrar la sesión y redirigir al login
            }
        }
    }
}
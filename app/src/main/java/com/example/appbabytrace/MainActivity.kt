package com.example.appbabytrace

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnrutadorApp()
        }
    }
}

@Composable
fun EnrutadorApp() {
    // Si ya hay una sesión activa en Firebase se entra directo a la pantalla principal
    var pantallaActual by remember {
        mutableStateOf(if (FirebaseAuth.getInstance().currentUser != null) "principal" else "login")
    }
    var correoPreCargado by remember { mutableStateOf("") }

    BackHandler(enabled = pantallaActual == "registro") {
        pantallaActual = "login"
    }

    when (pantallaActual) {
        "login" -> {
            PantallaLogin(
                onLoginExitoso = { pantallaActual = "principal" },
                onIrARegistro = { correoDigitado ->
                    correoPreCargado = correoDigitado
                    pantallaActual = "registro"
                }
            )
        }
        "registro" -> {
            PantallaRegistro(
                correoInicial = correoPreCargado,
                onRegistroExitoso = { pantallaActual = "principal" },
                onVolverLogin = { pantallaActual = "login" }
            )
        }
        "principal" -> {
            BabyTraceApp(onCerrarSesion = { pantallaActual = "login" })
        }
    }
}

// Opciones del menú lateral, con los colores pastel de la identidad visual del documento
private data class OpcionMenu(val ruta: String, val icono: String, val titulo: String, val color: Color)

private val opcionesMenu = listOf(
    OpcionMenu("inicio", "🏠", "Inicio", Color(0xFFE8F5E9)),
    OpcionMenu("historial", "🥣", "Seguimiento de Sólidos", Color(0xFFE3F2FD)),
    OpcionMenu("recetas", "📖", "Recetas Baby-Friendly", Color(0xFFE8F5E9)),
    OpcionMenu("progreso", "📅", "Plan de Introducción", Color(0xFFFBE9E7)),
    OpcionMenu("guia", "🔪", "Guía de Cortes", Color(0xFFFFF8E1)),
    OpcionMenu("usuarios", "👨‍👩‍👧", "Comunidad de Padres", Color(0xFFFFF3E0))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyTraceApp(onCerrarSesion: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Un único listener de Firestore compartido por todas las pantallas
    val datos = rememberDatosFamilia()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var registroEnEdicion by remember { mutableStateOf<RegistroComida?>(null) }
    var pestanaActual by remember { mutableStateOf("inicio") }

    var mostrarDialogoSalir by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    BackHandler(enabled = pestanaActual != "inicio") {
        pestanaActual = "inicio"
    }

    BackHandler(enabled = pestanaActual == "inicio") {
        mostrarDialogoSalir = true
    }

    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            title = { Text("Salir de la aplicación", color = Color(0xFF4CAF50)) },
            text = { Text("¿Estás seguro de que deseas salir de BabyTrace?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoSalir = false
                    (contexto as? Activity)?.finish()
                }) {
                    Text("Sí, salir", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSalir = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD7F2DE))
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👶", style = MaterialTheme.typography.displayMedium)
                    Text(datos.nombreBebe.ifEmpty { "BabyTrace" }, style = MaterialTheme.typography.titleLarge)
                    Text("Nutrición para Mi Bebé", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(12.dp))

                opcionesMenu.forEach { opcion ->
                    NavigationDrawerItem(
                        icon = { Text(opcion.icono, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(opcion.titulo) },
                        selected = pestanaActual == opcion.ruta,
                        onClick = {
                            pestanaActual = opcion.ruta
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = opcion.color,
                            selectedContainerColor = Color(0xFFA5D6A7)
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                    )
                }

                NavigationDrawerItem(
                    icon = { Text("🚪", style = MaterialTheme.typography.titleLarge) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onCerrarSesion()
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFFBE9E7)),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFFF4F6F8),
            topBar = {
                TopAppBar(
                    title = { Text(opcionesMenu.firstOrNull { it.ruta == pestanaActual }?.titulo?.takeIf { pestanaActual != "inicio" } ?: "BabyTrace") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text(text = "☰", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                )
            },
            floatingActionButton = {
                if (pestanaActual in listOf("inicio", "historial", "progreso")) {
                    FloatingActionButton(
                        onClick = {
                            registroEnEdicion = null
                            mostrarFormulario = true
                        },
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ) {
                        Text(text = "+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                when (pestanaActual) {
                    "inicio" -> PantallaInicio(datos)
                    "historial" -> PantallaHistorial(datos, onEditar = { registro ->
                        registroEnEdicion = registro
                        mostrarFormulario = true
                    })
                    "recetas" -> PantallaRecetas(datos)
                    "progreso" -> PantallaProgreso(datos)
                    "guia" -> PantallaGuiaCortes(datos)
                    "usuarios" -> PantallaUsuarios(onCerrarSesion = onCerrarSesion)
                }
            }
        }
    }

    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFormulario = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            // key: reinicia el estado del formulario al cambiar de registro
            key(registroEnEdicion?.id) {
                FormularioAlimento(datos = datos, registroExistente = registroEnEdicion, onCerrar = { mostrarFormulario = false })
            }
        }
    }
}

package com.example.appbabytrace

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var pantallaActual by remember { mutableStateOf("login") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyTraceApp(onCerrarSesion: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var mostrarFormulario by remember { mutableStateOf(false) }
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
            ModalDrawerSheet {
                Text(
                    text = "Menú BabyTrace",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4CAF50)
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Dashboard (Inicio)") },
                    selected = pestanaActual == "inicio",
                    onClick = {
                        pestanaActual = "inicio"
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Perfil Familiar (Usuarios)") },
                    selected = pestanaActual == "usuarios",
                    onClick = {
                        pestanaActual = "usuarios"
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Progreso de Alimentos") },
                    selected = pestanaActual == "progreso",
                    onClick = {
                        pestanaActual = "progreso"
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFFF4F6F8),
            topBar = {
                TopAppBar(
                    title = { Text("BabyTrace") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text(text = "☰", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                )
            },
            floatingActionButton = {
                if (pestanaActual == "inicio" || pestanaActual == "progreso") {
                    FloatingActionButton(
                        onClick = { mostrarFormulario = true },
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
                    "inicio" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text("Bienvenido al perfil de tu bebé.", style = MaterialTheme.typography.titleMedium)
                            Text("Hoy es un gran día para aprender a comer.", modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    "usuarios" -> {
                        PantallaUsuarios(onCerrarSesion = onCerrarSesion)
                    }
                    "progreso" -> {
                        PantallaProgreso()
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFormulario = false },
            containerColor = Color.White
        ) {
            FormularioAlimento(onCerrar = { mostrarFormulario = false })
        }
    }
}
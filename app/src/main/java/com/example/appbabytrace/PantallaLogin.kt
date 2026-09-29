package com.example.appbabytrace

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@Composable
fun PantallaLogin(
    onLoginExitoso: () -> Unit,
    onIrARegistro: (String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var errorCorreo by remember { mutableStateOf(false) }
    var errorContrasena by remember { mutableStateOf(false) }
    var mostrarPreguntaRegistro by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val contexto = LocalContext.current

    if (mostrarPreguntaRegistro) {
        AlertDialog(
            onDismissRequest = { mostrarPreguntaRegistro = false },
            title = { Text("Cuenta no encontrada", color = Color(0xFF4CAF50)) },
            text = { Text("Parece que este correo no está registrado o hubo un error. ¿Deseas crear una cuenta nueva?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarPreguntaRegistro = false
                    onIrARegistro(correo.trim())
                }) {
                    Text("Sí, registrarme")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPreguntaRegistro = false }) {
                    Text("Intentar de nuevo", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(containerColor = Color.White) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("BabyTrace", style = MaterialTheme.typography.displayMedium, color = Color(0xFF4CAF50))
            Text("Control y nutrición para tu bebé")
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = correo, onValueChange = { correo = it; errorCorreo = false },
                label = { Text("Correo electrónico") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = errorCorreo, supportingText = { if (errorCorreo) Text("Por favor, ingresa tu correo") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contrasena, onValueChange = { contrasena = it; errorContrasena = false },
                label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = errorContrasena, supportingText = { if (errorContrasena) Text("Por favor, ingresa tu contraseña") }
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val correoLimpio = correo.trim()
                        val contrasenaLimpia = contrasena.trim()

                        errorCorreo = correoLimpio.isEmpty()
                        errorContrasena = contrasenaLimpia.isEmpty()

                        if (errorCorreo || errorContrasena) return@Button

                        cargando = true

                        auth.signInWithEmailAndPassword(correoLimpio, contrasenaLimpia)
                            .addOnCompleteListener { tarea ->
                                cargando = false
                                if (tarea.isSuccessful) {
                                    Toast.makeText(contexto, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show()
                                    onLoginExitoso()
                                } else {
                                    val excepcion = tarea.exception
                                    val mensajeError = excepcion?.message ?: ""

                                    if (excepcion is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                                        excepcion is FirebaseAuthInvalidUserException ||
                                        mensajeError.contains("credential", ignoreCase = true)) {
                                        mostrarPreguntaRegistro = true
                                    } else {
                                        Toast.makeText(contexto, "Error: $mensajeError", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    }
                ) { Text("Ingresar") }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { onIrARegistro(correo.trim()) }) {
                    Text("¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
}
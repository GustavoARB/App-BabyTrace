package com.example.appbabytrace

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaUsuarios(
    onCerrarSesion: () -> Unit
) {
    var nombreBebe by remember { mutableStateOf("Cargando...") }
    var edadBebe by remember { mutableStateOf("") }
    var nombreCuidador by remember { mutableStateOf("") }
    var parentesco by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    var codigoInvitacion by remember { mutableStateOf("Cargando...") }

    var cuidadoresFamilia by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        val idUsuarioActual = auth.currentUser?.uid
        if (idUsuarioActual != null) {
            db.collection("Usuarios").document(idUsuarioActual).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        nombreBebe = documento.getString("nombreBebe") ?: "Información no registrada"
                        edadBebe = documento.getLong("edadBebeMeses")?.toString() ?: "0"

                        nombreCuidador = documento.getString("nombre") ?: "Sin nombre"
                        parentesco = documento.getString("parentesco") ?: ""
                        rol = documento.getString("rol") ?: ""
                        correo = documento.getString("correo") ?: ""

                        codigoInvitacion = documento.getString("codigoInvitacion") ?: "No disponible"

                        val miFamiliaId = documento.getString("familiaId") ?: ""

                        if (miFamiliaId.isNotEmpty()) {
                            db.collection("Usuarios")
                                .whereEqualTo("familiaId", miFamiliaId)
                                .get()
                                .addOnSuccessListener { resultados ->
                                    val listaTemporal = mutableListOf<Map<String, String>>()
                                    for (doc in resultados) {
                                        val mNombre = doc.getString("nombre") ?: ""
                                        val mParentesco = doc.getString("parentesco") ?: ""
                                        listaTemporal.add(mapOf("nombre" to mNombre, "parentesco" to mParentesco))
                                    }
                                    cuidadoresFamilia = listaTemporal
                                    cargando = false
                                }
                        } else {
                            cargando = false
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Centro Familiar", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(24.dp))

        if (cargando) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("👶 Bebé", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        if (rol == "Administrador") {
                            TextButton(onClick = { Toast.makeText(contexto, "Modo Edición próximamente", Toast.LENGTH_SHORT).show() }) {
                                Text("Editar")
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Nombre: $nombreBebe", style = MaterialTheme.typography.bodyLarge)
                    Text("Edad: $edadBebe meses", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("👤 Mi Perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { Toast.makeText(contexto, "Modo Edición próximamente", Toast.LENGTH_SHORT).show() }) {
                            Text("Editar")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text("Nombre: $nombreCuidador", style = MaterialTheme.typography.bodyLarge)
                    Text("Rol: $parentesco ($rol)", style = MaterialTheme.typography.bodyLarge)
                    Text("Correo: $correo", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👨‍👩‍👧 Red de Cuidadores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Código único de invitación:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    SelectionContainer {
                        Text(
                            text = codigoInvitacion,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            letterSpacing = 2.sp
                        )
                    }
                    Text("(Comparte este código para vincular familiares)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    cuidadoresFamilia.forEach { cuidador ->
                        Text("• ${cuidador["nombre"]} (${cuidador["parentesco"]})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = { auth.signOut(); onCerrarSesion() }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                Text("Cerrar Sesión")
            }
        }
    }
}
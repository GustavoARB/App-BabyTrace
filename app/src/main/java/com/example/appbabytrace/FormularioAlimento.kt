package com.example.appbabytrace

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FormularioAlimento(onCerrar: () -> Unit) {
    val catalogo = listOf(
        Pair("Manzana", "Frutas"), Pair("Pera", "Frutas"), Pair("Banano", "Frutas"), Pair("Mango", "Frutas"), Pair("Papaya", "Frutas"), Pair("Aguacate", "Frutas"), Pair("Fresa", "Frutas"),
        Pair("Zanahoria", "Verduras"), Pair("Brócoli", "Verduras"), Pair("Calabaza", "Verduras"), Pair("Espinaca", "Verduras"), Pair("Papa", "Verduras"), Pair("Batata", "Verduras"),
        Pair("Pollo", "Proteínas"), Pair("Carne de res", "Proteínas"), Pair("Huevo", "Proteínas"), Pair("Pescado", "Proteínas"), Pair("Lentejas", "Proteínas"), Pair("Frijoles", "Proteínas"),
        Pair("Avena", "Cereales"), Pair("Arroz", "Cereales"), Pair("Pasta", "Cereales"), Pair("Maíz", "Cereales")
    ).sortedBy { it.first }

    var textoBusqueda by remember { mutableStateOf("") }
    var menuExpandido by remember { mutableStateOf(false) }

    val alimentosFiltrados = catalogo.filter { it.first.contains(textoBusqueda, ignoreCase = true) }

    val metodos = listOf("Papilla", "BLW (Trozos)", "Líquido")
    var metodoSeleccionado by remember { mutableStateOf(metodos[0]) }
    var huboReaccion by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    val contexto = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Registrar Nueva Comida", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                menuExpandido = true
            },
            label = { Text("Buscar alimento...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (alimentosFiltrados.isNotEmpty() && menuExpandido && textoBusqueda.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    alimentosFiltrados.forEach { alimentoPair ->
                        TextButton(
                            onClick = {
                                textoBusqueda = alimentoPair.first
                                menuExpandido = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(alimentoPair.first, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                                Text(alimentoPair.second, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Método de presentación:", style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            metodos.forEach { metodo ->
                FilterChip(
                    selected = (metodo == metodoSeleccionado),
                    onClick = { metodoSeleccionado = metodo },
                    label = { Text(metodo) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿El bebé presentó alguna reacción?", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = huboReaccion, onCheckedChange = { huboReaccion = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50), checkedTrackColor = Color(0xFFC8E6C9))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onCerrar() }) { Text("Cancelar", color = Color.Gray) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val alimentoExiste = catalogo.any { it.first.equals(textoBusqueda, ignoreCase = true) }
                    if (textoBusqueda.isEmpty() || !alimentoExiste) {
                        Toast.makeText(contexto, "Por favor, selecciona un alimento de la lista", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    cargando = true
                    val idUsuarioActual = auth.currentUser?.uid

                    if (idUsuarioActual != null) {
                        db.collection("Usuarios").document(idUsuarioActual).get().addOnSuccessListener { doc ->
                            val familiaId = doc.getString("familiaId") ?: ""
                            val nombreRegistrador = doc.getString("nombre") ?: "Familiar"

                            val registroComida = hashMapOf(
                                "alimento" to textoBusqueda,
                                "metodo" to metodoSeleccionado,
                                "huboReaccion" to huboReaccion,
                                "fechaRegistro" to System.currentTimeMillis(),
                                "registradoPor" to nombreRegistrador
                            )

                            db.collection("Familias").document(familiaId).collection("HistorialAlimentos").add(registroComida)
                                .addOnSuccessListener {
                                    cargando = false
                                    Toast.makeText(contexto, "¡Comida registrada con éxito!", Toast.LENGTH_SHORT).show()
                                    onCerrar()
                                }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("Guardar Comida")
            }
        }
    }
}
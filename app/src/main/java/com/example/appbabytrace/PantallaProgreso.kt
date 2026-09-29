package com.example.appbabytrace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PantallaProgreso() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val catalogo = listOf(
        Pair("Manzana", "Frutas"), Pair("Pera", "Frutas"), Pair("Banano", "Frutas"), Pair("Mango", "Frutas"), Pair("Papaya", "Frutas"), Pair("Aguacate", "Frutas"), Pair("Fresa", "Frutas"),
        Pair("Zanahoria", "Verduras"), Pair("Brócoli", "Verduras"), Pair("Calabaza", "Verduras"), Pair("Espinaca", "Verduras"), Pair("Papa", "Verduras"), Pair("Batata", "Verduras"),
        Pair("Pollo", "Proteínas"), Pair("Carne de res", "Proteínas"), Pair("Huevo", "Proteínas"), Pair("Pescado", "Proteínas"), Pair("Lentejas", "Proteínas"), Pair("Frijoles", "Proteínas"),
        Pair("Avena", "Cereales"), Pair("Arroz", "Cereales"), Pair("Pasta", "Cereales"), Pair("Maíz", "Cereales")
    )

    val alimentosConsumidos = remember { mutableStateListOf<String>() }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("Usuarios").document(uid).get().addOnSuccessListener { doc ->
                val familiaId = doc.getString("familiaId") ?: ""
                if (familiaId.isNotEmpty()) {
                    db.collection("Familias").document(familiaId).collection("HistorialAlimentos")
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                val consumidos = snapshot.documents.mapNotNull { it.getString("alimento") }
                                alimentosConsumidos.clear()
                                alimentosConsumidos.addAll(consumidos)
                            }
                            cargando = false
                        }
                } else {
                    cargando = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Progreso de Alimentación", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
        Text("Controla qué ha probado tu bebé", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        if (cargando) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        } else {
            val categorias = catalogo.groupBy { it.second }

            categorias.forEach { (nombreCategoria, listaAlimentos) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(nombreCategoria, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        listaAlimentos.forEach { alimentoPair ->
                            val nombreAlimento = alimentoPair.first
                            val yaComio = alimentosConsumidos.contains(nombreAlimento)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = nombreAlimento,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (yaComio) Color.Black else Color.LightGray
                                )
                                Text(
                                    text = if (yaComio) "✅" else "⏳",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
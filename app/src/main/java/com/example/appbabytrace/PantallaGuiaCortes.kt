package com.example.appbabytrace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Guía de cortes y texturas seguras según la edad del bebé (RF08)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaGuiaCortes(datos: DatosFamilia) {
    val etapaDelBebe = etapaPorEdad(datos.edadBebeMeses)
    var etapaSeleccionada by remember(etapaDelBebe) { mutableStateOf(etapaDelBebe) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Guía de Cortes y Texturas", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
        Text(
            "${datos.nombreBebe.ifEmpty { "Tu bebé" }} tiene ${datos.edadBebeMeses} meses: etapa ${etapaDelBebe.nombre}",
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            etapasAlimentacion.forEach { etapa ->
                FilterChip(
                    selected = etapa == etapaSeleccionada,
                    onClick = { etapaSeleccionada = etapa },
                    label = { Text(etapa.rango) }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${etapaSeleccionada.nombre} (${etapaSeleccionada.rango})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Spacer(modifier = Modifier.height(8.dp))
                Text("🥣 Papilla", fontWeight = FontWeight.Bold)
                Text(etapaSeleccionada.papilla)
                Spacer(modifier = Modifier.height(8.dp))
                Text("✋ BLW (Baby-Led Weaning)", fontWeight = FontWeight.Bold)
                Text(etapaSeleccionada.blw)
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🚫 Siempre evita", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                alimentosAEvitar.forEach { Text("• $it") }
            }
        }

        Text("Cómo preparar cada alimento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        catalogoAlimentos.groupBy { it.categoria }.forEach { (categoria, alimentos) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(categoria, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    alimentos.forEach { alimento ->
                        consejosCorte[alimento.nombre]?.let { consejo ->
                            Text(alimento.nombre, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            Text(consejo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Text(
            "Esta guía es orientativa y no reemplaza la recomendación de tu pediatra.",
            style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

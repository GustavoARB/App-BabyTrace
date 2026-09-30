package com.example.appbabytrace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Recetas Baby-Friendly: solo muestra recetas cuyos ingredientes ya fueron tolerados (RF07)
@Composable
fun PantallaRecetas(datos: DatosFamilia) {
    val ahora = rememberAhora()
    val tolerados = ReglaTresDias.alimentosTolerados(datos.registros, ahora)
    val (disponibles, bloqueadas) = catalogoRecetas.partition { recetaDisponible(it, tolerados, datos.edadBebeMeses) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Recetas Baby-Friendly", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
        Text("Preparadas solo con alimentos que ${datos.nombreBebe.ifEmpty { "tu bebé" }} ya toleró", color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (datos.cargando) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
            return@Column
        }

        Text("Disponibles (${disponibles.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (disponibles.isEmpty()) {
            Text(
                "Todavía no hay recetas disponibles. A medida que registres alimentos y cumplan su periodo de prueba de 3 días sin reacciones, aparecerán aquí.",
                color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        disponibles.forEach { TarjetaReceta(it, tolerados, datos.edadBebeMeses, disponible = true) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Próximamente (${bloqueadas.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
        bloqueadas.forEach { TarjetaReceta(it, tolerados, datos.edadBebeMeses, disponible = false) }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaReceta(receta: Receta, tolerados: Set<String>, edadMeses: Int, disponible: Boolean) {
    var expandida by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(enabled = disponible) { expandida = !expandida },
        colors = CardDefaults.cardColors(containerColor = if (disponible) Color(0xFFFFF3E0) else Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(receta.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (disponible) Color.Black else Color.Gray)
            Text("${receta.metodo} · desde ${receta.edadMinimaMeses} meses", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Ingredientes: ${receta.ingredientes.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)

            if (!disponible) {
                val faltantes = receta.ingredientes.filter { it !in tolerados }
                val motivos = listOfNotNull(
                    "Falta tolerar: ${faltantes.joinToString(", ")}".takeIf { faltantes.isNotEmpty() },
                    "Disponible desde los ${receta.edadMinimaMeses} meses".takeIf { edadMeses < receta.edadMinimaMeses }
                )
                Text(motivos.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100))
            } else if (expandida) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                receta.pasos.forEachIndexed { i, paso -> Text("${i + 1}. $paso", style = MaterialTheme.typography.bodyMedium) }
            } else {
                Text("Toca para ver la preparación", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
            }
        }
    }
}

package com.example.appbabytrace

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

// Seguimiento de Sólidos: lectura, edición y eliminación del registro alimentario (RF03)
@Composable
fun PantallaHistorial(
    datos: DatosFamilia,
    onEditar: (RegistroComida) -> Unit
) {
    val contexto = LocalContext.current
    var registroAEliminar by remember { mutableStateOf<RegistroComida?>(null) }
    val formatoDia = remember { SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale.forLanguageTag("es")) }
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    registroAEliminar?.let { registro ->
        AlertDialog(
            onDismissRequest = { registroAEliminar = null },
            title = { Text("Eliminar registro") },
            text = { Text("¿Eliminar el registro de ${registro.alimento} del ${formatoDia.format(registro.fecha)}?") },
            confirmButton = {
                TextButton(onClick = {
                    val appContext = contexto.applicationContext
                    coleccionHistorial(datos.familiaId).document(registro.id).delete()
                        .addOnFailureListener { e -> Toast.makeText(appContext, "No se pudo eliminar: ${e.message}", Toast.LENGTH_LONG).show() }
                    registroAEliminar = null
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { registroAEliminar = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    if (datos.cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF4CAF50)) }
        return
    }

    val porDia = datos.registros.groupBy { formatoDia.format(it.fecha) }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Seguimiento de Sólidos", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
            Text("Historial de comidas de ${datos.nombreBebe.ifEmpty { "tu bebé" }}", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            if (datos.registros.isEmpty()) {
                Text("Aún no hay comidas registradas. Usa el botón + para agregar la primera.")
            }
        }

        porDia.forEach { (dia, registrosDelDia) ->
            item {
                Text(
                    dia.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(registrosDelDia, key = { it.id }) { registro ->
                // Administrador puede gestionar todo; cada cuidador puede eliminar lo que él registró
                val puedeEliminar = datos.esAdministrador || (registro.registradoPorUid.isNotEmpty() && registro.registradoPorUid == datos.uid)
                TarjetaRegistro(
                    registro = registro,
                    hora = formatoHora.format(registro.fecha),
                    puedeEliminar = puedeEliminar,
                    onEditar = { onEditar(registro) },
                    onEliminar = { registroAEliminar = registro }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun TarjetaRegistro(
    registro: RegistroComida,
    hora: String,
    puedeEliminar: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (registro.huboReaccion) Color(0xFFFFF3E0) else Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("$hora · ${registro.alimento}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (buscarAlimento(registro.alimento)?.esAlergeno == true) {
                    Text("Alérgeno", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                }
            }
            val detalle = listOf(registro.metodo, registro.cantidad).filter { it.isNotEmpty() }.joinToString(" · ")
            if (detalle.isNotEmpty()) Text(detalle, style = MaterialTheme.typography.bodyMedium)
            if (registro.huboReaccion) {
                val severidad = registro.severidad.ifEmpty { "Sin especificar" }
                val tipos = registro.tiposReaccion.joinToString(", ").ifEmpty { "sin detalle" }
                Text("⚠️ Reacción $severidad: $tipos", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB71C1C))
            }
            if (registro.registradoPor.isNotEmpty()) {
                Text("Registrado por ${registro.registradoPor}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEditar) { Text("Editar") }
                if (puedeEliminar) TextButton(onClick = onEliminar) { Text("Eliminar", color = Color.Red) }
            }
        }
    }
}

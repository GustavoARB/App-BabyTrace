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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Dashboard: perfil del bebé, banner de alertas y resumen de las comidas del día
@Composable
fun PantallaInicio(datos: DatosFamilia) {
    val ahora = rememberAhora()
    val formatoHora = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        if (datos.cargando) {
            CircularProgressIndicator(color = Color(0xFF4CAF50), modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("👶", style = MaterialTheme.typography.displaySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(datos.nombreBebe.ifEmpty { "Tu bebé" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${datos.edadBebeMeses} meses · ${etapaPorEdad(datos.edadBebeMeses).nombre}", color = Color.DarkGray)
                    Text("Hola, ${datos.nombreUsuario}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        BannerReglaTresDias(datos.registros, ahora)

        val tolerados = ReglaTresDias.alimentosTolerados(datos.registros, ahora).size
        val conReaccion = datos.registros.filter { it.huboReaccion }.map { it.alimento }.toSet().size
        val probados = datos.registros.map { it.alimento }.toSet().size
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaIndicador("Probados", "$probados", Color(0xFFE3F2FD), Modifier.weight(1f))
            TarjetaIndicador("Tolerados", "$tolerados", Color(0xFFE8F5E9), Modifier.weight(1f))
            TarjetaIndicador("Reacciones", "$conReaccion", Color(0xFFFFF3E0), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Comidas de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        val inicioDelDia = Calendar.getInstance().apply {
            timeInMillis = ahora
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val comidasHoy = datos.registros.filter { it.fecha >= inicioDelDia }

        if (comidasHoy.isEmpty()) {
            Text("Aún no hay comidas registradas hoy. Toca + para registrar una.", color = Color.Gray)
        }
        comidasHoy.forEach { registro ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatoHora.format(registro.fecha)}  ${registro.alimento}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (registro.huboReaccion) "⚠️ ${registro.severidad}" else registro.metodo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (registro.huboReaccion) Color(0xFFB71C1C) else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun TarjetaIndicador(titulo: String, valor: String, fondo: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = fondo)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

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

// Plan de Introducción: estado de cada alimento según la regla de los 3 días (RF05)
@Composable
fun PantallaProgreso(datos: DatosFamilia) {
    val ahora = rememberAhora()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Plan de Introducción", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF4CAF50))
        Text("Controla qué ha probado tu bebé", color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (datos.cargando) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
            return@Column
        }

        BannerReglaTresDias(datos.registros, ahora)
        Text(
            "✅ Tolerado   🧪 En prueba   ⚠️ Con reacción   ⏳ Sin probar",
            style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp)
        )

        catalogoAlimentos.groupBy { it.categoria }.forEach { (nombreCategoria, listaAlimentos) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(nombreCategoria, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    listaAlimentos.forEach { alimento ->
                        val estado = ReglaTresDias.estadoAlimento(alimento.nombre, datos.registros, ahora)
                        val detalle = when (estado) {
                            EstadoAlimento.EN_PRUEBA -> {
                                val inicio = ReglaTresDias.primeraIntroduccion(datos.registros)[alimento.nombre] ?: ahora
                                "faltan ${ReglaTresDias.horasHasta(inicio + ReglaTresDias.PERIODO_MS, ahora)} h"
                            }
                            else -> ""
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = alimento.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (estado == EstadoAlimento.NO_PROBADO) Color.LightGray else Color.Black
                                )
                                val etiquetas = listOfNotNull(
                                    "Alérgeno".takeIf { alimento.esAlergeno },
                                    detalle.ifEmpty { null }
                                )
                                if (etiquetas.isNotEmpty()) {
                                    Text(etiquetas.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100))
                                }
                            }
                            Text(text = iconoEstado(estado), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

fun iconoEstado(estado: EstadoAlimento) = when (estado) {
    EstadoAlimento.TOLERADO -> "✅"
    EstadoAlimento.EN_PRUEBA -> "🧪"
    EstadoAlimento.CON_REACCION -> "⚠️"
    EstadoAlimento.NO_PROBADO -> "⏳"
}

// Banner dinámico de alertas de la regla de los 3 días, usado en el Dashboard y en el Plan de Introducción
@Composable
fun BannerReglaTresDias(registros: List<RegistroComida>, ahora: Long) {
    val enPrueba = ReglaTresDias.alimentoEnPrueba(registros, ahora)
    if (enPrueba == null) {
        BannerAviso(
            "🟢 Puedes introducir un alimento nuevo hoy. Recuerda esperar 3 días antes del siguiente.",
            Color(0xFFE8F5E9), Color(0xFF2E7D32)
        )
    } else {
        val (alimento, fin) = enPrueba
        val horas = ReglaTresDias.horasHasta(fin, ahora)
        val tiempo = if (horas >= 24) "${horas / 24} d ${horas % 24} h" else "$horas h"
        BannerAviso(
            "🧪 Probando $alimento. Podrás introducir un alimento nuevo en $tiempo. Mientras tanto ofrece solo alimentos ya conocidos.",
            Color(0xFFFFF3E0), Color(0xFFE65100)
        )
    }
}

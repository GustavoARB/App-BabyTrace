package com.example.appbabytrace

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioAlimento(
    datos: DatosFamilia,
    registroExistente: RegistroComida? = null,
    onCerrar: () -> Unit
) {
    val catalogo = catalogoAlimentos.sortedBy { it.nombre }
    val esEdicion = registroExistente != null

    var textoBusqueda by remember { mutableStateOf(registroExistente?.alimento ?: "") }
    var menuExpandido by remember { mutableStateOf(false) }
    val alimentosFiltrados = catalogo.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }

    var metodoSeleccionado by remember { mutableStateOf(registroExistente?.metodo?.ifEmpty { null } ?: metodosPresentacion[0]) }
    var cantidadSeleccionada by remember { mutableStateOf(registroExistente?.cantidad?.ifEmpty { null } ?: cantidadesPorcion[1]) }
    var huboReaccion by remember { mutableStateOf(registroExistente?.huboReaccion ?: false) }
    var severidad by remember { mutableStateOf(registroExistente?.severidad?.ifEmpty { null } ?: severidadesReaccion[0]) }
    val tiposSeleccionados = remember { mutableStateListOf<String>().apply { addAll(registroExistente?.tiposReaccion ?: emptyList()) } }
    var fechaComida by remember { mutableLongStateOf(registroExistente?.fecha ?: System.currentTimeMillis()) }
    var mostrarConfirmacionRegla by remember { mutableStateOf(false) }

    val contexto = LocalContext.current
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // Para validar la regla de los 3 días se excluye el propio registro cuando se está editando
    val otrosRegistros = datos.registros.filter { it.id != registroExistente?.id }
    val alimentoElegido = buscarAlimento(textoBusqueda)
    val validacion = alimentoElegido?.let { ReglaTresDias.validarNuevoAlimento(it.nombre, otrosRegistros, fechaComida) }

    fun guardar() {
        val alimento = alimentoElegido ?: return
        if (datos.familiaId.isEmpty()) {
            Toast.makeText(contexto, "Tu usuario no está vinculado a una familia", Toast.LENGTH_LONG).show()
            return
        }

        val registro = hashMapOf<String, Any>(
            "alimento" to alimento.nombre,
            "metodo" to metodoSeleccionado,
            "cantidad" to cantidadSeleccionada,
            "huboReaccion" to huboReaccion,
            "severidad" to if (huboReaccion) severidad else "",
            "tiposReaccion" to if (huboReaccion) tiposSeleccionados.toList() else emptyList<String>(),
            "fechaRegistro" to fechaComida
        )
        val coleccion = coleccionHistorial(datos.familiaId)
        val appContext = contexto.applicationContext
        val onError: (Exception) -> Unit = { e ->
            Toast.makeText(appContext, "No se pudo sincronizar la comida: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // No se espera la respuesta del servidor: Firestore guarda en caché local y sincroniza al recuperar la red (RNF03)
        if (esEdicion) {
            coleccion.document(registroExistente!!.id).update(registro).addOnFailureListener(onError)
            Toast.makeText(contexto, "Registro actualizado", Toast.LENGTH_SHORT).show()
        } else {
            registro["registradoPor"] = datos.nombreUsuario
            registro["registradoPorUid"] = datos.uid
            coleccion.add(registro).addOnFailureListener(onError)
            Toast.makeText(contexto, "¡Comida registrada con éxito!", Toast.LENGTH_SHORT).show()
        }
        onCerrar()
    }

    if (mostrarConfirmacionRegla && validacion is ResultadoValidacion.Bloqueado) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionRegla = false },
            title = { Text("Regla de los 3 días", color = Color(0xFFE65100)) },
            text = {
                Text(
                    "${validacion.alimentoEnPrueba} sigue en periodo de prueba (faltan ${validacion.horasRestantes} h). " +
                        "Introducir otro alimento nuevo ahora dificulta identificar cuál causó una posible reacción. ¿Registrar de todas formas?"
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarConfirmacionRegla = false; guardar() }) {
                    Text("Registrar de todas formas", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionRegla = false }) { Text("Esperar", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(if (esEdicion) "Editar Comida" else "Registrar Nueva Comida", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CAF50))
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
                    alimentosFiltrados.forEach { alimento ->
                        val estado = ReglaTresDias.estadoAlimento(alimento.nombre, otrosRegistros, fechaComida)
                        TextButton(
                            onClick = {
                                textoBusqueda = alimento.nombre
                                menuExpandido = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(alimento.nombre, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                                val detalle = buildString {
                                    append(alimento.categoria)
                                    if (alimento.esAlergeno) append(" · Alérgeno")
                                    if (estado == EstadoAlimento.NO_PROBADO) append(" · Nuevo")
                                }
                                Text(detalle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        // Aviso de la regla de los 3 días (RF05)
        when (validacion) {
            is ResultadoValidacion.Bloqueado -> BannerAviso(
                "⚠️ ${validacion.alimentoEnPrueba} está en periodo de prueba. Espera ${validacion.horasRestantes} h antes de introducir ${alimentoElegido?.nombre}.",
                Color(0xFFFFE0B2), Color(0xFFE65100)
            )
            is ResultadoValidacion.Permitido -> BannerAviso(
                "🆕 Alimento nuevo: inicia su periodo de prueba de 3 días. Observa al bebé por posibles reacciones." +
                    if (alimentoElegido?.esAlergeno == true) " Es un alérgeno común." else "",
                Color(0xFFE3F2FD), Color(0xFF1565C0)
            )
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Fecha y hora", style = MaterialTheme.typography.bodyMedium)
                Text(formatoFecha.format(fechaComida), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = fechaComida }
                DatePickerDialog(contexto, { _, anio, mes, dia ->
                    cal.set(anio, mes, dia)
                    TimePickerDialog(contexto, { _, hora, minuto ->
                        cal.set(Calendar.HOUR_OF_DAY, hora)
                        cal.set(Calendar.MINUTE, minuto)
                        fechaComida = cal.timeInMillis.coerceAtMost(System.currentTimeMillis())
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                    datePicker.maxDate = System.currentTimeMillis()
                }.show()
            }) { Text("Cambiar") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Método de presentación:", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            metodosPresentacion.forEach { metodo ->
                FilterChip(selected = metodo == metodoSeleccionado, onClick = { metodoSeleccionado = metodo }, label = { Text(metodo) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Cantidad:", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cantidadesPorcion.forEach { cantidad ->
                FilterChip(selected = cantidad == cantidadSeleccionada, onClick = { cantidadSeleccionada = cantidad }, label = { Text(cantidad) })
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

        // Registro escalado de reacciones (RF04)
        if (huboReaccion) {
            Text("Severidad:", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                severidadesReaccion.forEach { nivel ->
                    FilterChip(
                        selected = nivel == severidad, onClick = { severidad = nivel }, label = { Text(nivel) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colorSeveridad(nivel))
                    )
                }
            }
            Text("Tipo de reacción:", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tiposReaccion.forEach { tipo ->
                    FilterChip(
                        selected = tipo in tiposSeleccionados,
                        onClick = { if (tipo in tiposSeleccionados) tiposSeleccionados.remove(tipo) else tiposSeleccionados.add(tipo) },
                        label = { Text(tipo) }
                    )
                }
            }
            if (severidad == "Grave") {
                BannerAviso("🚨 Ante una reacción grave (dificultad para respirar, hinchazón de cara o labios) busca atención médica de inmediato.", Color(0xFFFFCDD2), Color(0xFFB71C1C))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onCerrar() }) { Text("Cancelar", color = Color.Gray) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    when {
                        alimentoElegido == null ->
                            Toast.makeText(contexto, "Por favor, selecciona un alimento de la lista", Toast.LENGTH_SHORT).show()
                        huboReaccion && tiposSeleccionados.isEmpty() ->
                            Toast.makeText(contexto, "Selecciona al menos un tipo de reacción", Toast.LENGTH_SHORT).show()
                        validacion is ResultadoValidacion.Bloqueado -> mostrarConfirmacionRegla = true
                        else -> guardar()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(if (esEdicion) "Guardar Cambios" else "Guardar Comida")
            }
        }
    }
}

@Composable
fun BannerAviso(texto: String, fondo: Color, colorTexto: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Text(texto, modifier = Modifier.padding(12.dp), color = colorTexto, style = MaterialTheme.typography.bodyMedium)
    }
}

fun colorSeveridad(severidad: String): Color = when (severidad) {
    "Grave" -> Color(0xFFFFCDD2)
    "Moderada" -> Color(0xFFFFE0B2)
    else -> Color(0xFFFFF9C4)
}

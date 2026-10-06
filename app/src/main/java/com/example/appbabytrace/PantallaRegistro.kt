package com.example.appbabytrace

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    correoInicial: String = "",
    onRegistroExitoso: () -> Unit,
    onVolverLogin: () -> Unit
) {
    BackHandler { onVolverLogin() }

    var crearNuevaFamilia by remember { mutableStateOf(true) }
    var codigoFamilia by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf("") }
    var parentesco by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf(correoInicial) }
    var contrasena by remember { mutableStateOf("") }

    var nombreBebe by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var generoBebe by remember { mutableStateOf("") }

    var menuParentesco by remember { mutableStateOf(false) }
    var menuGenero by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    val opcionesParentesco = listOf("Madre", "Padre", "Abuelo/a", "Tío/a", "Niñero/a", "Otro")
    val opcionesGenero = listOf("Masculino", "Femenino")

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val contexto = LocalContext.current

    val calendario = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        contexto,
        { _, year, month, dayOfMonth -> fechaNacimiento = "$dayOfMonth/${month + 1}/$year" },
        calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Registro") },
                navigationIcon = {
                    IconButton(onClick = { onVolverLogin() }) {
                        Text("⬅️", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (crearNuevaFamilia) "Creando nueva familia" else "Unirse a familia", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CAF50))
                Switch(checked = crearNuevaFamilia, onCheckedChange = { crearNuevaFamilia = it })
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            if (!crearNuevaFamilia) {
                Text("Pídele el código de 6 caracteres al creador de la familia", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = codigoFamilia,
                    onValueChange = { codigoFamilia = it.uppercase() },
                    label = { Text("Código de Invitación (Ej: A7B9X2)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (crearNuevaFamilia) {
                Text("👶 Datos del Bebé", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(value = nombreBebe, onValueChange = { nombreBebe = it }, label = { Text("Nombre del bebé") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fechaNacimiento, onValueChange = {}, readOnly = true,
                    label = { Text("Fecha de nacimiento") }, modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                    enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = menuGenero, onExpandedChange = { menuGenero = !menuGenero }) {
                    OutlinedTextField(
                        value = generoBebe, onValueChange = {}, readOnly = true, label = { Text("Género") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuGenero) }, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = menuGenero, onDismissRequest = { menuGenero = false }) {
                        opcionesGenero.forEach { seleccion ->
                            DropdownMenuItem(text = { Text(seleccion) }, onClick = { generoBebe = seleccion; menuGenero = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text("👤 Datos del Cuidador", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Tu nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = menuParentesco, onExpandedChange = { menuParentesco = !menuParentesco }) {
                OutlinedTextField(
                    value = parentesco, onValueChange = {}, readOnly = true, label = { Text("Parentesco / Rol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuParentesco) }, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = menuParentesco, onDismissRequest = { menuParentesco = false }) {
                    opcionesParentesco.forEach { seleccion ->
                        DropdownMenuItem(text = { Text(seleccion) }, onClick = { parentesco = seleccion; menuParentesco = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña (mín 6)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))

            if (cargando) CircularProgressIndicator()
            else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val cor = correo.trim()
                        val pass = contrasena.trim()
                        val codigoInvitacion = codigoFamilia.trim()

                        val mensajeValidacion = when {
                            !crearNuevaFamilia && codigoInvitacion.length != 6 -> "El código de invitación debe tener 6 caracteres"
                            crearNuevaFamilia && nombreBebe.isBlank() -> "Ingresa el nombre del bebé"
                            crearNuevaFamilia && fechaNacimiento.isEmpty() -> "Selecciona la fecha de nacimiento"
                            crearNuevaFamilia && generoBebe.isEmpty() -> "Selecciona el género del bebé"
                            nombre.isBlank() -> "Ingresa tu nombre"
                            parentesco.isEmpty() -> "Selecciona tu parentesco"
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(cor).matches() -> "Ingresa un correo válido"
                            pass.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                            else -> null
                        }
                        if (mensajeValidacion != null) {
                            Toast.makeText(contexto, mensajeValidacion, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        cargando = true
                        val onError: (Exception) -> Unit = { e ->
                            cargando = false
                            Toast.makeText(contexto, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                        if (!crearNuevaFamilia) {
                            // Se crea la cuenta primero para que la consulta del código se haga ya autenticado
                            auth.createUserWithEmailAndPassword(cor, pass).addOnSuccessListener { res ->
                                val usuario = res.user!!
                                val cancelarRegistro: (String) -> Unit = { mensaje ->
                                    usuario.delete()
                                    cargando = false
                                    Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show()
                                }

                                db.collection("Usuarios").whereEqualTo("codigoInvitacion", codigoInvitacion).get()
                                    .addOnSuccessListener { docs ->
                                        if (docs.isEmpty) {
                                            cancelarRegistro("El código no existe o es incorrecto")
                                            return@addOnSuccessListener
                                        }

                                        val rolesTomados = docs.mapNotNull { it.getString("parentesco") }
                                        if (rolesTomados.contains(parentesco)) {
                                            cancelarRegistro("El rol de $parentesco ya está ocupado en esta familia")
                                            return@addOnSuccessListener
                                        }

                                        val docCreador = docs.documents[0]
                                        val datos = hashMapOf(
                                            "nombreBebe" to (docCreador.getString("nombreBebe") ?: ""),
                                            "edadBebeMeses" to (docCreador.getLong("edadBebeMeses") ?: 0),
                                            "generoBebe" to (docCreador.getString("generoBebe") ?: ""),
                                            "nombre" to nombre.trim(),
                                            "parentesco" to parentesco,
                                            "correo" to cor,
                                            "rol" to "Lector/Editor",
                                            "familiaId" to (docCreador.getString("familiaId") ?: ""),
                                            "codigoInvitacion" to codigoInvitacion
                                        )
                                        db.collection("Usuarios").document(usuario.uid).set(datos)
                                            .addOnSuccessListener { onRegistroExitoso() }
                                            .addOnFailureListener { e -> cancelarRegistro("Error: ${e.message}") }
                                    }
                                    .addOnFailureListener { e -> cancelarRegistro("Error: ${e.message}") }
                            }.addOnFailureListener(onError)
                        } else {
                            val edadMeses = calcularEdadMeses(fechaNacimiento) ?: 0

                            val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                            val nuevoCodigoCorto = (1..6).map { caracteres.random() }.joinToString("")

                            auth.createUserWithEmailAndPassword(cor, pass).addOnSuccessListener { res ->
                                val nuevoId = res.user!!.uid
                                val datos = hashMapOf(
                                    "nombreBebe" to nombreBebe,
                                    "edadBebeMeses" to edadMeses,
                                    "fechaNacimiento" to fechaNacimiento,
                                    "generoBebe" to generoBebe,
                                    "nombre" to nombre.trim(),
                                    "parentesco" to parentesco,
                                    "correo" to cor,
                                    "rol" to "Administrador",
                                    "familiaId" to nuevoId,
                                    "codigoInvitacion" to nuevoCodigoCorto
                                )
                                db.collection("Usuarios").document(nuevoId).set(datos)
                                    .addOnSuccessListener { onRegistroExitoso() }
                                    .addOnFailureListener(onError)
                            }.addOnFailureListener(onError)
                        }
                    }
                ) { Text(if (crearNuevaFamilia) "Registrar Familia" else "Unirme a la Familia") }
            }
        }
    }
}
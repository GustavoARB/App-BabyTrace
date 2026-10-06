package com.example.appbabytrace

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// Estado compartido por las pantallas principales: usuario, bebé e historial de comidas de la familia
data class DatosFamilia(
    val cargando: Boolean = true,
    val uid: String = "",
    val familiaId: String = "",
    val nombreUsuario: String = "",
    val rol: String = "",
    val nombreBebe: String = "",
    val edadBebeMeses: Int = 0,
    val registros: List<RegistroComida> = emptyList()
) {
    val esAdministrador get() = rol == "Administrador"
}

fun documentoARegistro(doc: DocumentSnapshot): RegistroComida? {
    val alimento = doc.getString("alimento") ?: return null
    return RegistroComida(
        id = doc.id,
        alimento = alimento,
        metodo = doc.getString("metodo") ?: "",
        cantidad = doc.getString("cantidad") ?: "",
        fecha = doc.getLong("fechaRegistro") ?: 0L,
        huboReaccion = doc.getBoolean("huboReaccion") ?: false,
        severidad = doc.getString("severidad") ?: "",
        tiposReaccion = (doc.get("tiposReaccion") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        registradoPor = doc.getString("registradoPor") ?: "",
        registradoPorUid = doc.getString("registradoPorUid") ?: ""
    )
}

fun coleccionHistorial(familiaId: String) =
    FirebaseFirestore.getInstance().collection("Familias").document(familiaId).collection("HistorialAlimentos")

// Hora actual que se refresca cada minuto para que las cuentas regresivas de la regla de 3 días avancen solas
@Composable
fun rememberAhora(): Long {
    val ahora by produceState(System.currentTimeMillis()) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
            value = System.currentTimeMillis()
        }
    }
    return ahora
}

// Escucha en tiempo real el historial de la familia (sincronización familiar) y lo libera al salir
@Composable
fun rememberDatosFamilia(): DatosFamilia {
    var datos by remember { mutableStateOf(DatosFamilia()) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        var listener: ListenerRegistration? = null
        var activo = true

        if (uid == null) {
            datos = datos.copy(cargando = false)
        } else {
            db.collection("Usuarios").document(uid).get().addOnSuccessListener { docUsuario ->
                if (!activo) return@addOnSuccessListener
                val familiaId = docUsuario.getString("familiaId") ?: ""
                datos = datos.copy(
                    uid = uid,
                    familiaId = familiaId,
                    nombreUsuario = docUsuario.getString("nombre") ?: "Familiar",
                    rol = docUsuario.getString("rol") ?: "",
                    nombreBebe = docUsuario.getString("nombreBebe") ?: "",
                    edadBebeMeses = docUsuario.getLong("edadBebeMeses")?.toInt() ?: 0
                )
                if (familiaId.isEmpty()) {
                    datos = datos.copy(cargando = false)
                    return@addOnSuccessListener
                }

                // El documento del creador de la familia tiene la fecha de nacimiento: así la edad siempre está al día
                db.collection("Usuarios").document(familiaId).get().addOnSuccessListener { docCreador ->
                    val edad = docCreador.getString("fechaNacimiento")?.let { calcularEdadMeses(it) }
                    if (edad != null && activo) datos = datos.copy(edadBebeMeses = edad)
                }

                listener = coleccionHistorial(familiaId).addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        datos = datos.copy(
                            registros = snapshot.documents.mapNotNull { documentoARegistro(it) }.sortedByDescending { it.fecha }
                        )
                    }
                    datos = datos.copy(cargando = false)
                }
            }.addOnFailureListener { datos = datos.copy(cargando = false) }
        }

        onDispose {
            activo = false
            listener?.remove()
        }
    }
    return datos
}

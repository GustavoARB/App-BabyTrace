package com.example.appbabytrace

import java.util.Calendar

// Registro de una comida del bebé (RF03 / RF04). Se mantiene sin dependencias de Android para poder probarlo
data class RegistroComida(
    val id: String,
    val alimento: String,
    val metodo: String,
    val cantidad: String,
    val fecha: Long,
    val huboReaccion: Boolean,
    val severidad: String = "",
    val tiposReaccion: List<String> = emptyList(),
    val registradoPor: String = "",
    val registradoPorUid: String = ""
)

enum class EstadoAlimento { NO_PROBADO, EN_PRUEBA, TOLERADO, CON_REACCION }

sealed class ResultadoValidacion {
    // El alimento ya fue introducido antes, se puede registrar sin restricción
    object YaIntroducido : ResultadoValidacion()
    // Es un alimento nuevo y no hay otro en periodo de prueba
    object Permitido : ResultadoValidacion()
    // Es un alimento nuevo pero otro alimento sigue dentro de su periodo de 3 días
    data class Bloqueado(val alimentoEnPrueba: String, val horasRestantes: Long) : ResultadoValidacion()
}

// Motor de Reglas y Alertas (RF05): ventanas de 72 horas por cada alimento nuevo
object ReglaTresDias {
    const val HORAS_PERIODO = 72L
    const val PERIODO_MS = HORAS_PERIODO * 60 * 60 * 1000
    private const val HORA_MS = 60 * 60 * 1000L

    // Fecha de la primera vez que se registró cada alimento
    fun primeraIntroduccion(registros: List<RegistroComida>): Map<String, Long> =
        registros.groupBy { it.alimento }.mapValues { (_, lista) -> lista.minOf { it.fecha } }

    // Alimento nuevo cuyo periodo de prueba sigue activo (el más reciente) y el momento en que termina
    fun alimentoEnPrueba(registros: List<RegistroComida>, ahora: Long): Pair<String, Long>? =
        primeraIntroduccion(registros)
            .filter { (_, inicio) -> inicio <= ahora && ahora - inicio < PERIODO_MS }
            .maxByOrNull { it.value }
            ?.let { (alimento, inicio) -> alimento to inicio + PERIODO_MS }

    fun estadoAlimento(alimento: String, registros: List<RegistroComida>, ahora: Long): EstadoAlimento {
        val delAlimento = registros.filter { it.alimento == alimento }
        if (delAlimento.isEmpty()) return EstadoAlimento.NO_PROBADO
        if (delAlimento.any { it.huboReaccion }) return EstadoAlimento.CON_REACCION
        val inicio = delAlimento.minOf { it.fecha }
        return if (ahora - inicio < PERIODO_MS) EstadoAlimento.EN_PRUEBA else EstadoAlimento.TOLERADO
    }

    fun alimentosTolerados(registros: List<RegistroComida>, ahora: Long): Set<String> =
        registros.map { it.alimento }.toSet()
            .filter { estadoAlimento(it, registros, ahora) == EstadoAlimento.TOLERADO }
            .toSet()

    fun validarNuevoAlimento(alimento: String, registros: List<RegistroComida>, ahora: Long): ResultadoValidacion {
        if (registros.any { it.alimento == alimento }) return ResultadoValidacion.YaIntroducido
        val (enPrueba, fin) = alimentoEnPrueba(registros, ahora) ?: return ResultadoValidacion.Permitido
        return ResultadoValidacion.Bloqueado(enPrueba, horasHasta(fin, ahora))
    }

    // Horas que faltan redondeadas hacia arriba, para no mostrar "0 h" cuando aún quedan minutos
    fun horasHasta(fin: Long, ahora: Long): Long = ((fin - ahora) + HORA_MS - 1) / HORA_MS
}

// Edad en meses cumplidos a partir de una fecha "d/M/yyyy"; null si el formato no es válido
fun calcularEdadMeses(fechaNacimiento: String, hoy: Calendar = Calendar.getInstance()): Int? {
    val partes = fechaNacimiento.split("/").mapNotNull { it.trim().toIntOrNull() }
    if (partes.size != 3) return null
    val (dia, mes, anio) = partes
    var meses = (hoy.get(Calendar.YEAR) - anio) * 12 + (hoy.get(Calendar.MONTH) + 1 - mes)
    if (hoy.get(Calendar.DAY_OF_MONTH) < dia) meses--
    return meses.coerceAtLeast(0)
}

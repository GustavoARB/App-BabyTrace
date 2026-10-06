package com.example.appbabytrace

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ReglaTresDiasTest {
    private val hora = 60 * 60 * 1000L
    private val ahora = 1_000_000 * hora

    private fun registro(alimento: String, horasAtras: Long, reaccion: Boolean = false) = RegistroComida(
        id = "$alimento-$horasAtras", alimento = alimento, metodo = "Papilla", cantidad = "",
        fecha = ahora - horasAtras * hora, huboReaccion = reaccion
    )

    @Test
    fun sinRegistros_permiteAlimentoNuevo() {
        assertEquals(ResultadoValidacion.Permitido, ReglaTresDias.validarNuevoAlimento("Pera", emptyList(), ahora))
    }

    @Test
    fun alimentoNuevoDentroDe72h_bloqueaOtroAlimentoNuevo() {
        val registros = listOf(registro("Manzana", 24))
        val resultado = ReglaTresDias.validarNuevoAlimento("Pera", registros, ahora)
        assertEquals(ResultadoValidacion.Bloqueado("Manzana", 48), resultado)
    }

    @Test
    fun despuesDe72h_permiteAlimentoNuevo() {
        val registros = listOf(registro("Manzana", 72))
        assertEquals(ResultadoValidacion.Permitido, ReglaTresDias.validarNuevoAlimento("Pera", registros, ahora))
    }

    @Test
    fun alimentoYaIntroducido_noSeBloquea() {
        val registros = listOf(registro("Manzana", 5), registro("Banano", 200))
        assertEquals(ResultadoValidacion.YaIntroducido, ReglaTresDias.validarNuevoAlimento("Banano", registros, ahora))
    }

    @Test
    fun elPeriodoSeCuentaDesdeLaPrimeraVez() {
        // Repetir la manzana no reinicia su periodo de prueba
        val registros = listOf(registro("Manzana", 80), registro("Manzana", 2))
        assertNull(ReglaTresDias.alimentoEnPrueba(registros, ahora))
        assertEquals(EstadoAlimento.TOLERADO, ReglaTresDias.estadoAlimento("Manzana", registros, ahora))
    }

    @Test
    fun estados_deLosAlimentos() {
        val registros = listOf(registro("Manzana", 100), registro("Huevo", 90, reaccion = true), registro("Pera", 10))
        assertEquals(EstadoAlimento.TOLERADO, ReglaTresDias.estadoAlimento("Manzana", registros, ahora))
        assertEquals(EstadoAlimento.CON_REACCION, ReglaTresDias.estadoAlimento("Huevo", registros, ahora))
        assertEquals(EstadoAlimento.EN_PRUEBA, ReglaTresDias.estadoAlimento("Pera", registros, ahora))
        assertEquals(EstadoAlimento.NO_PROBADO, ReglaTresDias.estadoAlimento("Mango", registros, ahora))
        assertEquals(setOf("Manzana"), ReglaTresDias.alimentosTolerados(registros, ahora))
    }

    @Test
    fun horasRestantes_redondeaHaciaArriba() {
        assertEquals(1, ReglaTresDias.horasHasta(ahora + 1, ahora))
        assertEquals(2, ReglaTresDias.horasHasta(ahora + 2 * hora, ahora))
    }

    @Test
    fun recetas_requierenIngredientesToleradosYEdad() {
        val receta = Receta("Prueba", listOf("Manzana", "Pera"), 8, "Papilla", emptyList())
        assertTrue(recetaDisponible(receta, setOf("Manzana", "Pera", "Banano"), 8))
        assertFalse(recetaDisponible(receta, setOf("Manzana"), 10))
        assertFalse(recetaDisponible(receta, setOf("Manzana", "Pera"), 7))
    }

    @Test
    fun recetas_soloUsanAlimentosDelCatalogo() {
        val nombres = catalogoAlimentos.map { it.nombre }.toSet()
        catalogoRecetas.forEach { receta -> assertTrue(receta.nombre, nombres.containsAll(receta.ingredientes)) }
    }

    @Test
    fun edadEnMeses() {
        val hoy = Calendar.getInstance().apply { set(2026, Calendar.SEPTEMBER, 29) }
        assertEquals(8, calcularEdadMeses("15/1/2026", hoy))
        assertEquals(7, calcularEdadMeses("30/1/2026", hoy))
        assertEquals(0, calcularEdadMeses("1/10/2026", hoy))
        assertNull(calcularEdadMeses("fecha inválida", hoy))
    }

    @Test
    fun etapas_segunEdad() {
        assertEquals("0-5 meses", etapaPorEdad(4).rango)
        assertEquals("6-8 meses", etapaPorEdad(6).rango)
        assertEquals("9-11 meses", etapaPorEdad(11).rango)
        assertEquals("12+ meses", etapaPorEdad(18).rango)
    }
}

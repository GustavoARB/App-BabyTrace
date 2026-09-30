package com.example.appbabytrace

// Alimento del catálogo (ver Diagrama de Clases: Alimento con nombre y esAlergeno)
data class Alimento(
    val nombre: String,
    val categoria: String,
    val esAlergeno: Boolean = false
)

// Catálogo compartido de alimentos. Los alérgenos comunes se marcan para resaltarlos en la regla de los 3 días
val catalogoAlimentos = listOf(
    Alimento("Manzana", "Frutas"), Alimento("Pera", "Frutas"), Alimento("Banano", "Frutas"), Alimento("Mango", "Frutas"),
    Alimento("Papaya", "Frutas"), Alimento("Aguacate", "Frutas"), Alimento("Fresa", "Frutas"),
    Alimento("Zanahoria", "Verduras"), Alimento("Brócoli", "Verduras"), Alimento("Calabaza", "Verduras"),
    Alimento("Espinaca", "Verduras"), Alimento("Papa", "Verduras"), Alimento("Batata", "Verduras"),
    Alimento("Pollo", "Proteínas"), Alimento("Carne de res", "Proteínas"), Alimento("Huevo", "Proteínas", esAlergeno = true),
    Alimento("Pescado", "Proteínas", esAlergeno = true), Alimento("Lentejas", "Proteínas"), Alimento("Frijoles", "Proteínas"),
    Alimento("Yogur natural", "Lácteos", esAlergeno = true), Alimento("Queso fresco", "Lácteos", esAlergeno = true),
    Alimento("Avena", "Cereales"), Alimento("Arroz", "Cereales"), Alimento("Pasta", "Cereales", esAlergeno = true), Alimento("Maíz", "Cereales")
)

fun buscarAlimento(nombre: String): Alimento? =
    catalogoAlimentos.firstOrNull { it.nombre.equals(nombre.trim(), ignoreCase = true) }

val metodosPresentacion = listOf("Papilla", "BLW (Trozos)", "Líquido")
val cantidadesPorcion = listOf("Probadita", "1-2 cucharadas", "Media porción", "Porción completa")
val severidadesReaccion = listOf("Leve", "Moderada", "Grave")
val tiposReaccion = listOf("Sarpullido", "Malestar digestivo", "Vómito", "Diarrea", "Hinchazón", "Otro")

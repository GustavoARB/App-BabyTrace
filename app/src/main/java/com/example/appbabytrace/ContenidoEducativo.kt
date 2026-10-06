package com.example.appbabytrace

// Base de conocimiento: recetas (RF07) y guía de cortes y texturas por edad (RF08).
// Contenido orientativo: siempre debe primar la indicación del pediatra.

data class Receta(
    val nombre: String,
    val ingredientes: List<String>,
    val edadMinimaMeses: Int,
    val metodo: String,
    val pasos: List<String>
)

val catalogoRecetas = listOf(
    Receta("Puré de calabaza y zanahoria", listOf("Calabaza", "Zanahoria"), 6, "Papilla",
        listOf("Pela y corta la calabaza y la zanahoria en cubos.", "Cocina al vapor 15-20 min hasta que estén muy blandas.", "Tritura con un poco del agua de cocción hasta lograr una textura lisa.")),
    Receta("Compota de manzana y pera", listOf("Manzana", "Pera"), 6, "Papilla",
        listOf("Pela y retira semillas.", "Cocina al vapor 10 min.", "Tritura sin agregar azúcar.")),
    Receta("Bastones de batata al horno", listOf("Batata"), 6, "BLW (Trozos)",
        listOf("Corta la batata en bastones del tamaño de un dedo adulto.", "Hornea a 200 °C por 25 min, sin sal.", "Verifica que se aplasten fácilmente entre tus dedos.")),
    Receta("Crema de aguacate y banano", listOf("Aguacate", "Banano"), 6, "Papilla",
        listOf("Aplasta medio aguacate maduro y medio banano.", "Mezcla hasta obtener una crema suave.", "Sirve de inmediato para evitar que se oxide.")),
    Receta("Tortitas de banano y avena", listOf("Banano", "Avena"), 6, "BLW (Trozos)",
        listOf("Aplasta un banano maduro y mezcla con 4 cucharadas de avena.", "Forma tortitas pequeñas.", "Cocina en sartén antiadherente 2-3 min por lado.")),
    Receta("Avena cocida con fresas", listOf("Avena", "Fresa"), 6, "Papilla",
        listOf("Cocina la avena en agua hasta que espese.", "Agrega fresas aplastadas.", "Deja entibiar antes de servir.")),
    Receta("Pollo desmechado con papa y brócoli", listOf("Pollo", "Papa", "Brócoli"), 7, "BLW (Trozos)",
        listOf("Cocina el pollo en agua hasta que esté bien cocido y desméchalo.", "Cocina la papa en bastones y el brócoli en floretes al vapor.", "Sirve el brócoli con su tallo como agarradera.")),
    Receta("Lentejas con arroz y zanahoria", listOf("Lentejas", "Arroz", "Zanahoria"), 8, "Papilla",
        listOf("Cocina las lentejas con la zanahoria picada hasta que se deshagan.", "Agrega arroz cocido.", "Machaca dejando grumos suaves según la edad.")),
    Receta("Tortilla de espinaca", listOf("Huevo", "Espinaca"), 8, "BLW (Trozos)",
        listOf("Bate un huevo y mezcla con espinaca cocida y picada fina.", "Cocina a fuego bajo hasta que el huevo esté completamente cuajado.", "Corta en tiras.")),
    Receta("Pescado con puré de papa", listOf("Pescado", "Papa"), 8, "Papilla",
        listOf("Cocina el pescado al vapor y retira todas las espinas.", "Prepara un puré de papa sin sal.", "Desmenuza el pescado y mézclalo con el puré.")),
    Receta("Yogur natural con mango", listOf("Yogur natural", "Mango"), 8, "Papilla",
        listOf("Usa yogur entero sin azúcar.", "Agrega mango maduro aplastado.", "Mezcla y sirve frío.")),
    Receta("Pasta con salsa de calabaza y queso", listOf("Pasta", "Calabaza", "Queso fresco"), 9, "BLW (Trozos)",
        listOf("Cocina pasta de forma grande (penne o fusilli) hasta que esté blanda.", "Tritura calabaza cocida con queso fresco para la salsa.", "Mezcla y deja entibiar.")),
    Receta("Albóndigas de res y frijol", listOf("Carne de res", "Frijoles"), 9, "BLW (Trozos)",
        listOf("Mezcla carne molida con frijoles cocidos y aplastados.", "Forma albóndigas alargadas.", "Hornea 20 min hasta que estén bien cocidas por dentro.")),
    Receta("Arepitas de maíz con queso", listOf("Maíz", "Queso fresco"), 10, "BLW (Trozos)",
        listOf("Mezcla harina de maíz con agua tibia y queso fresco rallado.", "Forma arepitas delgadas.", "Asa a fuego medio y corta en tiras."))
)

// Una receta está disponible cuando el bebé tiene la edad mínima y ya toleró todos sus ingredientes
fun recetaDisponible(receta: Receta, tolerados: Set<String>, edadMeses: Int): Boolean =
    edadMeses >= receta.edadMinimaMeses && tolerados.containsAll(receta.ingredientes)

data class EtapaAlimentacion(
    val nombre: String,
    val rango: String,
    val papilla: String,
    val blw: String
)

val etapasAlimentacion = listOf(
    EtapaAlimentacion(
        "Antes de la alimentación complementaria", "0-5 meses",
        "La leche materna o de fórmula cubre todas sus necesidades. Consulta al pediatra antes de iniciar sólidos.",
        "El BLW se inicia hacia los 6 meses, cuando el bebé se sienta con apoyo y lleva objetos a la boca."
    ),
    EtapaAlimentacion(
        "Inicio", "6-8 meses",
        "Purés lisos y sin grumos, con textura de yogur espeso. Ofrece con cuchara pequeña.",
        "Trozos del tamaño de un dedo adulto (5-6 cm), bien cocidos: deben aplastarse entre tu pulgar e índice."
    ),
    EtapaAlimentacion(
        "Exploración", "9-11 meses",
        "Machacados con grumos suaves y picados finos para practicar la masticación.",
        "Trozos pequeños del tamaño de un garbanzo para practicar el agarre de pinza."
    ),
    EtapaAlimentacion(
        "Mesa familiar", "12+ meses",
        "Comida de la familia picada, con textura normal pero blanda.",
        "Trozos pequeños o comida de la familia adaptada; puede usar cubiertos con ayuda."
    )
)

fun etapaPorEdad(edadMeses: Int): EtapaAlimentacion = when {
    edadMeses < 6 -> etapasAlimentacion[0]
    edadMeses < 9 -> etapasAlimentacion[1]
    edadMeses < 12 -> etapasAlimentacion[2]
    else -> etapasAlimentacion[3]
}

// Recomendaciones específicas de corte y preparación por alimento del catálogo
val consejosCorte = mapOf(
    "Manzana" to "Cruda es dura y puede causar atragantamiento: cocínala al vapor hasta que se aplaste o rállala fina.",
    "Pera" to "Si está madura y blanda, en gajos sin piel; si no, al vapor.",
    "Banano" to "Partido a lo largo en tiras, o dejando parte de la cáscara como agarradera.",
    "Mango" to "Maduro, en tiras anchas; es resbaloso, puedes pasarlo por avena molida para mejor agarre.",
    "Papaya" to "Madura, en tiras gruesas o aplastada.",
    "Aguacate" to "En tiras gruesas; rebózalas en avena molida para que no se resbalen.",
    "Fresa" to "Aplastada o en cuartos. Nunca fresas pequeñas enteras.",
    "Zanahoria" to "Nunca cruda en trozos. Cocida en bastones blandos o en puré.",
    "Brócoli" to "Floretes al vapor dejando el tallo como mango.",
    "Calabaza" to "Cocida en bastones o en puré.",
    "Espinaca" to "Cocida y picada fina dentro de purés o tortillas: las hojas enteras se pegan al paladar.",
    "Papa" to "Cocida en bastones o puré, sin sal.",
    "Batata" to "Asada o al vapor en bastones.",
    "Pollo" to "Muy bien cocido. Tiras grandes para chupar al inicio, luego desmechado.",
    "Carne de res" to "Tiras grandes para chupar al inicio; molida o desmenuzada después.",
    "Huevo" to "Siempre bien cocido, sin yema líquida. En tortilla en tiras o duro en cuartos. Alérgeno: introdúcelo solo.",
    "Pescado" to "Sin espinas, bien cocido y desmenuzado. Evita pescados altos en mercurio (pez espada, atún grande). Alérgeno.",
    "Lentejas" to "Muy bien cocidas y aplastadas, en puré o en tortitas.",
    "Frijoles" to "Cocidos y aplastados; enteros pueden causar atragantamiento.",
    "Yogur natural" to "Entero y sin azúcar. En cuchara precargada para que el bebé la tome. Alérgeno (leche).",
    "Queso fresco" to "Pasteurizado y bajo en sal, en tiras o rallado. Alérgeno (leche).",
    "Avena" to "Cocida en papilla o en tortitas.",
    "Arroz" to "Bien cocido y blando; en bolitas aplastadas para BLW.",
    "Pasta" to "Formas grandes (penne, fusilli) bien cocidas. Contiene trigo (gluten).",
    "Maíz" to "Los granos enteros son riesgo de atragantamiento: triturado o en arepa blanda."
)

val alimentosAEvitar = listOf(
    "Miel antes de los 12 meses (riesgo de botulismo).",
    "Frutos secos y maní enteros; uvas, tomates cherry y salchichas enteras o en rodajas.",
    "Sal y azúcar añadidas.",
    "Leche de vaca como bebida principal antes de los 12 meses."
)

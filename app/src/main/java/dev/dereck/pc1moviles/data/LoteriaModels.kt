package dev.dereck.pc1moviles.data

/** Identificador del sorteo de prueba usado en la PC01. */
const val SORTEO_ID = "sorteo_prueba"

/** Estados válidos del sorteo según el enunciado. */
object SorteoEstado {
    const val ABIERTO = "abierto"
    const val EN_CURSO = "en_curso"
    const val FINALIZADO = "finalizado"
}

data class Sorteo(
    val id: String = SORTEO_ID,
    val nombre: String = "Sorteo de prueba",
    val fecha: String = "",
    val estado: String = SorteoEstado.ABIERTO
)

data class Jugada(
    val sorteoId: String = SORTEO_ID,
    val uid: String = "",
    val numeros: List<Int> = emptyList()
)

/** Valida una jugada: exactamente 6 números distintos entre 1 y 45. */
fun validarJugada(numeros: Collection<Int>): String? {
    if (numeros.size != 6) return "Debes elegir exactamente 6 números."
    if (numeros.any { it !in 1..45 }) return "Los números deben estar entre 1 y 45."
    if (numeros.toSet().size != 6) return "Los 6 números deben ser distintos."
    return null
}

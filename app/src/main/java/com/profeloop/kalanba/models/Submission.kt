package com.profeloop.kalanba.models

data class Submission(
    val id: String = "",
    val tareaId: String = "",
    val estudianteUid: String = "",
    val estudianteNombre: String = "",
    val profesorUid: String = "",
    val asignatura: String = "",
    val grado: Int = 0,
    val periodo: Int = 0,
    val archivoUrl: String = "",
    val archivoNombre: String = "",
    val estado: String = "enviada",  // "enviada", "revisando", "calificada"
    val calificacion: Double = 0.0,
    val comentarioProfesor: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val revisadoAt: Long = 0L
) {
    fun estadoLabel(): String = when (estado) {
        "enviada"     -> "Enviada"
        "revisando"   -> "En revisión"
        "calificada"  -> "Calificada"
        else          -> estado.replaceFirstChar { it.uppercase() }
    }
}

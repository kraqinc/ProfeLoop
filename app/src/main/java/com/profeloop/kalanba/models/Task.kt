package com.profeloop.kalanba.models

data class Task(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val asignatura: String = "",
    val grado: Int = 0,
    val periodo: Int = 0,           // 1-4
    val profesorUid: String = "",
    val profesorNombre: String = "",
    val archivoUrl: String = "",
    val archivoNombre: String = "",
    val archivoTipo: String = "",   // "pdf", "docx", "xlsx"
    val fechaLimite: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val activa: Boolean = true
) {
    fun gradoStr(): String = "$grado°"
    fun periodoStr(): String = "Período $periodo"
}

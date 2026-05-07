package com.profeloop.kalanba.models

data class Task(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val grado: Int = 0,
    val asignatura: String = "",
    val periodo: Int = 1,
    val archivoUrl: String = "",
    val archivoNombre: String = "",
    val fechaLimite: Long = 0L,
    val fechaCreacion: Long = 0L
)

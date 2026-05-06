package com.tareaapp.models

data class AppNotification(
    val id: String = "",
    val destinatarioUid: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "",       // "tarea_enviada", "revisando", "calificada", "mensaje"
    val tareaId: String = "",
    val submissionId: String = "",
    val leida: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

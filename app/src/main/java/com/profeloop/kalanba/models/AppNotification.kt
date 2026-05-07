package com.profeloop.kalanba.models

data class AppNotification(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "",
    val leida: Boolean = false,
    val timestamp: Long = 0L,
    val targetUserId: String = ""
)

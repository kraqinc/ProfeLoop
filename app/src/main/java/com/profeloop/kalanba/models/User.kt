package com.profeloop.kalanba.models

data class User(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val rol: String = "",           // "estudiante" o "profesor"
    val nivel: String = "",         // "primaria" o "bachillerato"
    val grado: Int = 0,             // 1-9
    val asignaturas: List<String> = emptyList(),
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun nombreCompleto(): String = "$nombre $apellido"
    fun esProfesor(): Boolean = rol == "profesor"
    fun esEstudiante(): Boolean = rol == "estudiante"
    fun gradoStr(): String = "$grado°"
}

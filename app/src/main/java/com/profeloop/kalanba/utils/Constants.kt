package com.profeloop.kalanba.utils

object Constants {

    // Firestore Collections
    const val COLLECTION_USERS         = "usuarios"
    const val COLLECTION_TASKS         = "tareas"
    const val COLLECTION_SUBMISSIONS   = "entregas"
    const val COLLECTION_SUBJECTS      = "asignaturas"
    const val COLLECTION_NOTIFICATIONS = "notificaciones"

    // User Roles
    const val ROL_ESTUDIANTE = "estudiante"
    const val ROL_PROFESOR   = "profesor"

    // Niveles
    const val NIVEL_PRIMARIA      = "primaria"
    const val NIVEL_BACHILLERATO  = "bachillerato"

    // Grades
    val GRADOS_PRIMARIA      = listOf(1, 2, 3, 4, 5)
    val GRADOS_BACHILLERATO  = listOf(6, 7, 8, 9)

    // Periods
    val PERIODOS = listOf(1, 2, 3, 4)

    // Submission States
    const val ESTADO_ENVIADA    = "enviada"
    const val ESTADO_REVISANDO  = "revisando"
    const val ESTADO_CALIFICADA = "calificada"

    // Notification Types
    const val NOTIF_TAREA_ENVIADA = "tarea_enviada"
    const val NOTIF_REVISANDO     = "revisando"
    const val NOTIF_CALIFICADA    = "calificada"
    const val NOTIF_MENSAJE       = "mensaje"

    // FCM Topics
    fun topicGrado(grado: Int) = "grado_$grado"
    fun topicAsignatura(grado: Int, asignatura: String) =
        "grado_${grado}_${asignatura.lowercase().replace(" ", "_")}"

    // Notification Channel
    const val CHANNEL_ID   = "tareaapp_channel"
    const val CHANNEL_NAME = "ProfeLoop Notificaciones"

    // Intent Extras
    const val EXTRA_GRADO      = "extra_grado"
    const val EXTRA_ASIGNATURA = "extra_asignatura"
    const val EXTRA_PERIODO    = "extra_periodo"
    const val EXTRA_TAREA_ID   = "extra_tarea_id"
    const val EXTRA_SUBMISSION_ID = "extra_submission_id"

    // SharedPreferences
    const val PREFS_NAME     = "tareaapp_prefs"
    const val PREF_USER_UID  = "user_uid"
    const val PREF_USER_ROL  = "user_rol"
}

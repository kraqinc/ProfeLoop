package com.tareaapp.tasks

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FieldValue
import com.tareaapp.databinding.ActivityPublishTaskBinding
import com.tareaapp.models.Task
import com.tareaapp.models.User
import com.tareaapp.utils.Constants
import com.tareaapp.utils.FirebaseUtils
import com.tareaapp.utils.gone
import com.tareaapp.utils.visible
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PublishTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishTaskBinding
    private var fileUri: Uri? = null
    private var fechaLimite: Long = 0L
    private var grado: Int      = 8
    private var asignatura: String = ""
    private var periodo: Int    = 1
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Publicar Tarea"

        grado      = intent.getIntExtra(Constants.EXTRA_GRADO, 8)
        asignatura = intent.getStringExtra(Constants.EXTRA_ASIGNATURA) ?: ""
        periodo    = intent.getIntExtra(Constants.EXTRA_PERIODO, 1)

        binding.tvInfoTarea.text = "$asignatura · Grado $grado° · Período $periodo"

        loadUser()
        setupListeners()
    }

    private fun loadUser() {
        lifecycleScope.launch {
            val uid  = FirebaseUtils.currentUid ?: return@launch
            val user = FirebaseUtils.getUserProfile(uid) ?: return@launch
            currentUser = user

            // Check if subject already claimed by another teacher
            val teacher = FirebaseUtils.getSubjectTeacher(grado, asignatura)
            if (teacher != null && teacher.uid != uid) {
                Toast.makeText(
                    this@PublishTaskActivity,
                    "Ya hay un/a profesor/a en esta asignatura",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }

            // If first time claiming, add subject to teacher's profile
            if (teacher == null) {
                val newSubjects = user.asignaturas.toMutableList()
                if (!newSubjects.contains(asignatura)) {
                    newSubjects.add(asignatura)
                    FirebaseUtils.db.collection(Constants.COLLECTION_USERS)
                        .document(uid)
                        .update("asignaturas", newSubjects, "grado", grado)
                        .await()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSeleccionarArchivo.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
            }
            startActivityForResult(Intent.createChooser(intent, "Selecciona archivo"), REQUEST_FILE)
        }

        binding.btnFechaLimite.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day, 23, 59, 0)
                    fechaLimite = cal.timeInMillis
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
                    binding.btnFechaLimite.text = "📅 ${sdf.format(cal.time)}"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnPublicar.setOnClickListener { publishTask() }
    }

    private fun publishTask() {
        val titulo      = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "Ingresa un título"
            return
        }
        if (descripcion.isEmpty()) {
            binding.tilDescripcion.error = "Ingresa una descripción"
            return
        }
        if (fileUri == null) {
            Toast.makeText(this, "Selecciona un archivo para la tarea", Toast.LENGTH_SHORT).show()
            return
        }
        if (fechaLimite == 0L) {
            Toast.makeText(this, "Selecciona la fecha límite", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tilTitulo.error     = null
        binding.tilDescripcion.error = null
        binding.progressBar.visible()
        binding.btnPublicar.isEnabled = false

        lifecycleScope.launch {
            try {
                val uid      = FirebaseUtils.currentUid ?: return@launch
                val fileName = "tarea_${System.currentTimeMillis()}_${getFileName(fileUri!!)}"
                val ref      = FirebaseUtils.storage.reference.child("tareas/$grado/$asignatura/$periodo/$fileName")

                ref.putFile(fileUri!!).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                val extension   = getExtension(fileUri!!)

                val task = Task(
                    titulo          = titulo,
                    descripcion     = descripcion,
                    asignatura      = asignatura,
                    grado           = grado,
                    periodo         = periodo,
                    profesorUid     = uid,
                    profesorNombre  = currentUser.nombreCompleto(),
                    archivoUrl      = downloadUrl,
                    archivoNombre   = fileName,
                    archivoTipo     = extension,
                    fechaLimite     = fechaLimite
                )

                FirebaseUtils.publishTask(task)

                binding.progressBar.gone()
                Toast.makeText(this@PublishTaskActivity, "¡Tarea publicada exitosamente!", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                binding.progressBar.gone()
                binding.btnPublicar.isEnabled = true
                Toast.makeText(this@PublishTaskActivity, "Error al publicar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK) {
            fileUri = data?.data
            val name = getFileName(fileUri!!)
            binding.tvArchivoSeleccionado.text = "📎 $name"
            binding.tvArchivoSeleccionado.visible()
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if (idx >= 0) it.getString(idx) else "archivo"
        } ?: "archivo"
    }

    private fun getExtension(uri: Uri): String {
        val mime = contentResolver.getType(uri) ?: return "pdf"
        return when (mime) {
            "application/pdf" -> "pdf"
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
            else -> "pdf"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val REQUEST_FILE = 1002
    }
}

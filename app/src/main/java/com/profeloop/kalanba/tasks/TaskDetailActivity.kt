package com.profeloop.kalanba.tasks

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.profeloop.kalanba.R
import com.profeloop.kalanba.databinding.ActivityTaskDetailBinding
import com.profeloop.kalanba.models.AppNotification
import com.profeloop.kalanba.models.Submission
import com.profeloop.kalanba.models.Task
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import com.profeloop.kalanba.utils.gone
import com.profeloop.kalanba.utils.toFormattedDate
import com.profeloop.kalanba.utils.visible
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private var tareaId: String    = ""
    private var task: Task?        = null
    private var submission: Submission? = null
    private var isProfesor: Boolean    = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle de Tarea"

        tareaId = intent.getStringExtra(Constants.EXTRA_TAREA_ID) ?: ""
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            binding.progressBar.visible()
            val uid  = FirebaseUtils.currentUid ?: return@launch
            val user = FirebaseUtils.getUserProfile(uid)
            isProfesor = user?.esProfesor() == true

            // Load task
            val doc = FirebaseUtils.db.collection(Constants.COLLECTION_TASKS)
                .document(tareaId).get().await()
            task = doc.toObject(Task::class.java)?.copy(id = doc.id)

            if (task == null) {
                binding.progressBar.gone()
                Toast.makeText(this@TaskDetailActivity, "Tarea no encontrada", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            // Load submission if student
            if (!isProfesor) {
                submission = FirebaseUtils.getStudentSubmissionForTask(tareaId, uid)
            }

            binding.progressBar.gone()
            renderTask(task!!, user?.nombreCompleto() ?: "")
        }
    }

    private fun renderTask(t: Task, userName: String) {
        binding.tvTaskTitle.text    = t.titulo
        binding.tvDescripcion.text  = t.descripcion
        binding.tvProfesor.text     = "Publicado por: ${t.profesorNombre}"
        binding.tvFechaLimite.text  = "Fecha límite: ${t.fechaLimite.toFormattedDate()}"
        binding.tvAsignatura.text   = "${t.asignatura} · Grado ${t.grado}° · Período ${t.periodo}"

        val fileIcon = when (t.archivoTipo) {
            "pdf"  -> "📄 PDF"
            "docx", "doc" -> "📝 Word"
            "xlsx", "xls" -> "📊 Excel"
            else -> "📎 Archivo"
        }
        binding.tvArchivoTarea.text = "$fileIcon: ${t.archivoNombre}"

        binding.btnDescargarTarea.setOnClickListener { downloadFile(t.archivoUrl, t.archivoNombre) }

        if (isProfesor) {
            setupProfesorView()
        } else {
            setupEstudianteView(t, userName)
        }
    }

    private fun setupProfesorView() {
        binding.cardSubmit.gone()
        binding.cardSubmission.gone()
        binding.tvVerEntregas.visible()
        binding.tvVerEntregas.setOnClickListener {
            lifecycleScope.launch {
                val subs = FirebaseUtils.getSubmissionsForTask(tareaId)
                showSubmissionsDialog(subs)
            }
        }
    }

    private fun setupEstudianteView(t: Task, studentName: String) {
        binding.tvVerEntregas.gone()
        val sub = submission
        if (sub != null) {
            // Already submitted
            binding.cardSubmit.gone()
            binding.cardSubmission.visible()
            binding.tvEstadoEntrega.text   = "Estado: ${sub.estadoLabel()}"
            binding.tvArchivoEntrega.text  = "Archivo: ${sub.archivoNombre}"
            if (sub.estado == Constants.ESTADO_CALIFICADA) {
                binding.tvCalificacion.visible()
                binding.tvCalificacion.text  = "Calificación: ${sub.calificacion}"
                binding.tvComentario.visible()
                binding.tvComentario.text    = "Comentario: ${sub.comentarioProfesor}"
            }
        } else {
            binding.cardSubmission.gone()
            binding.cardSubmit.visible()
            binding.btnSubirEntrega.setOnClickListener {
                openFilePicker()
            }
        }
    }

    private fun openFilePicker() {
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
        startActivityForResult(Intent.createChooser(intent, "Selecciona tu tarea"), REQUEST_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            uploadSubmission(uri)
        }
    }

    private fun uploadSubmission(uri: Uri) {
        lifecycleScope.launch {
            binding.progressBar.visible()
            binding.btnSubirEntrega.isEnabled = false
            try {
                val uid      = FirebaseUtils.currentUid ?: return@launch
                val user     = FirebaseUtils.getUserProfile(uid) ?: return@launch
                val t        = task ?: return@launch
                val fileName = "entrega_${uid}_${System.currentTimeMillis()}.${getExtension(uri)}"
                val ref      = FirebaseUtils.storage.reference.child("entregas/$tareaId/$fileName")

                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                val sub = Submission(
                    tareaId         = tareaId,
                    estudianteUid   = uid,
                    estudianteNombre = user.nombreCompleto(),
                    profesorUid     = t.profesorUid,
                    asignatura      = t.asignatura,
                    grado           = t.grado,
                    periodo         = t.periodo,
                    archivoUrl      = downloadUrl,
                    archivoNombre   = fileName,
                    estado          = Constants.ESTADO_ENVIADA
                )
                val subId = FirebaseUtils.submitTask(sub)

                // Notify teacher via Firestore notification
                val profesorUser = FirebaseUtils.getUserProfile(t.profesorUid)
                val notif = AppNotification(
                    destinatarioUid = t.profesorUid,
                    titulo          = "Nueva entrega recibida",
                    mensaje         = "El/La estudiante ${user.nombreCompleto()} envió su tarea lista de ${t.asignatura}",
                    tipo            = Constants.NOTIF_TAREA_ENVIADA,
                    tareaId         = tareaId,
                    submissionId    = subId
                )
                FirebaseUtils.saveNotification(notif)

                binding.progressBar.gone()
                submission = sub
                setupEstudianteView(t, user.nombreCompleto())
                Toast.makeText(this@TaskDetailActivity, "¡Tarea enviada exitosamente!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                binding.progressBar.gone()
                binding.btnSubirEntrega.isEnabled = true
                Toast.makeText(this@TaskDetailActivity, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getExtension(uri: Uri): String {
        val mime = contentResolver.getType(uri) ?: return "pdf"
        return when (mime) {
            "application/pdf"  -> "pdf"
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
            else -> "pdf"
        }
    }

    private fun showSubmissionsDialog(subs: List<Submission>) {
        if (subs.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Entregas")
                .setMessage("Aún no hay entregas para esta tarea.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val items = subs.map { "${it.estudianteNombre} — ${it.estadoLabel()}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Entregas (${subs.size})")
            .setItems(items) { _, idx ->
                showGradeDialog(subs[idx])
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun showGradeDialog(sub: Submission) {
        val t = task ?: return
        lifecycleScope.launch {
            // Mark as reviewing
            FirebaseUtils.updateSubmissionStatus(sub.id, Constants.ESTADO_REVISANDO)
            val profesorUser = FirebaseUtils.getUserProfile(FirebaseUtils.currentUid ?: "")

            // Notify student
            val notif = AppNotification(
                destinatarioUid = sub.estudianteUid,
                titulo          = "Tu tarea está siendo revisada",
                mensaje         = "Hola, el profesor/a ${profesorUser?.nombreCompleto()} está revisando tu tarea de ${t.asignatura}",
                tipo            = Constants.NOTIF_REVISANDO,
                tareaId         = tareaId,
                submissionId    = sub.id
            )
            FirebaseUtils.saveNotification(notif)

            // Show grade dialog
            val dialogView = layoutInflater.inflate(R.layout.dialog_grade_submission, null)
            val etCalif  = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCalificacion)
            val etComment = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etComentario)

            AlertDialog.Builder(this@TaskDetailActivity)
                .setTitle("Calificar a ${sub.estudianteNombre}")
                .setView(dialogView)
                .setPositiveButton("Guardar") { _, _ ->
                    val calif   = etCalif.text.toString().toDoubleOrNull() ?: 0.0
                    val comment = etComment.text.toString().trim()
                    gradeSubmission(sub, calif, comment, t.asignatura)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun gradeSubmission(sub: Submission, calificacion: Double, comentario: String, asignatura: String) {
        lifecycleScope.launch {
            FirebaseUtils.gradeSubmission(sub.id, calificacion, comentario)

            val notif = AppNotification(
                destinatarioUid = sub.estudianteUid,
                titulo          = "¡Tu tarea fue calificada!",
                mensaje         = "Recibiste ${calificacion} en $asignatura. Comentario: $comentario",
                tipo            = Constants.NOTIF_CALIFICADA,
                tareaId         = tareaId,
                submissionId    = sub.id
            )
            FirebaseUtils.saveNotification(notif)
            Toast.makeText(this@TaskDetailActivity, "Calificación guardada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("Descargando tarea...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, "Descargando $fileName...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback: open in browser
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val REQUEST_FILE = 1001
    }
}

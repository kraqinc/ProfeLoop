package com.profeloop.kalanba.tasks

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.profeloop.kalanba.databinding.ActivityPublishTaskBinding
import com.profeloop.kalanba.models.AppNotification
import com.profeloop.kalanba.models.Task
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PublishTaskActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GRADO = "extra_grado"
        const val EXTRA_ASIGNATURA = "extra_asignatura"
    }

    private lateinit var binding: ActivityPublishTaskBinding
    private var selectedFileUri: Uri? = null
    private var selectedDeadlineMs: Long = 0L
    private val calendar = Calendar.getInstance()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            val fileName = uri.lastPathSegment ?: "archivo"
            binding.tvSelectedFile.text = fileName
            binding.tvSelectedFile.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupPeriodoSpinner()

        binding.btnDeadline.setOnClickListener { showDatePicker() }
        binding.btnAttachFile.setOnClickListener { filePickerLauncher.launch("*/*") }
        binding.btnPublish.setOnClickListener { publishTask() }
    }

    private fun setupPeriodoSpinner() {
        val periodos = listOf("Período 1", "Período 2", "Período 3", "Período 4")
        binding.spinnerPeriodo.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, periodos
        )
    }

    private fun showDatePicker() {
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            selectedDeadlineMs = calendar.timeInMillis
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.tvDeadlineSelected.text = sdf.format(Date(selectedDeadlineMs))
            binding.tvDeadlineSelected.visibility = View.VISIBLE
            binding.btnDeadline.text = "Fecha: ${binding.tvDeadlineSelected.text}"
        }
        DatePickerDialog(
            this, listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun publishTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val periodo = binding.spinnerPeriodo.selectedItemPosition + 1
        val grado = intent.getIntExtra(EXTRA_GRADO, 0)
        val asignatura = intent.getStringExtra(EXTRA_ASIGNATURA) ?: ""

        if (title.isEmpty()) {
            binding.tilTitle.error = "El título es obligatorio"
            return
        }
        if (selectedDeadlineMs == 0L) {
            Toast.makeText(this, "Selecciona una fecha límite", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnPublish.isEnabled = false

        lifecycleScope.launch {
            try {
                val uid = FirebaseUtils.currentUid ?: return@launch
                val user = FirebaseUtils.getUserProfile(uid) ?: return@launch

                var fileUrl = ""
                var fileName = ""
                selectedFileUri?.let { uri ->
                    fileName = uri.lastPathSegment ?: "archivo"
                    val ref = FirebaseUtils.storage.reference
                        .child("tasks/$uid/${System.currentTimeMillis()}/$fileName")
                    ref.putFile(uri).await()
                    fileUrl = ref.downloadUrl.await().toString()
                }

                val task = Task(
                    titulo = title,
                    descripcion = description,
                    profesorId = uid,
                    profesorNombre = user.nombre,
                    grado = grado,
                    asignatura = asignatura,
                    periodo = periodo,
                    archivoUrl = fileUrl,
                    archivoNombre = fileName,
                    fechaLimite = selectedDeadlineMs,
                    fechaCreacion = System.currentTimeMillis()
                )

                val success = FirebaseUtils.publishTask(task)
                if (success) {
                    // Notify students of this grade
                    val students = FirebaseUtils.db.collection(Constants.COLLECTION_USERS)
                        .whereEqualTo("grado", grado)
                        .whereEqualTo("rol", Constants.ROL_ESTUDIANTE)
                        .get().await().toObjects(com.profeloop.kalanba.models.User::class.java)

                    for (student in students) {
                        FirebaseUtils.sendNotification(
                            AppNotification(
                                titulo = "Nueva tarea publicada",
                                mensaje = "$asignatura: $title",
                                tipo = "new_task",
                                timestamp = System.currentTimeMillis(),
                                targetUserId = student.uid
                            )
                        )
                    }
                    Toast.makeText(this@PublishTaskActivity, "Tarea publicada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@PublishTaskActivity, "Error al publicar tarea", Toast.LENGTH_SHORT).show()
                    binding.btnPublish.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@PublishTaskActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnPublish.isEnabled = true
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

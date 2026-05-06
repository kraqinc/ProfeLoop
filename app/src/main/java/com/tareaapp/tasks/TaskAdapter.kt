package com.tareaapp.tasks

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tareaapp.databinding.ItemTaskBinding
import com.tareaapp.models.Task
import com.tareaapp.utils.toDateOnly
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private val isProfesor: Boolean,
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
            override fun areContentsTheSame(a: Task, b: Task) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text    = task.titulo
            binding.tvProfesor.text     = "Prof. ${task.profesorNombre}"
            binding.tvFechaLimite.text  = "Entrega: ${task.fechaLimite.toDateOnly()}"

            val fileIcon = when (task.archivoTipo.lowercase()) {
                "pdf"  -> "📄"
                "docx", "doc" -> "📝"
                "xlsx", "xls" -> "📊"
                else   -> "📎"
            }
            binding.tvArchivoTipo.text = "$fileIcon ${task.archivoNombre}"

            // Color urgency indicator
            val daysLeft = TimeUnit.MILLISECONDS.toDays(task.fechaLimite - System.currentTimeMillis())
            val urgencyColor = when {
                daysLeft < 0  -> Color.parseColor("#F44336") // Vencida
                daysLeft <= 2 -> Color.parseColor("#FF9800") // Urgente
                daysLeft <= 7 -> Color.parseColor("#FFC107") // Pronto
                else          -> Color.parseColor("#4CAF50") // Normal
            }
            binding.urgencyBar.setBackgroundColor(urgencyColor)

            val daysText = when {
                daysLeft < 0  -> "Vencida"
                daysLeft == 0L -> "¡Vence hoy!"
                daysLeft == 1L -> "Vence mañana"
                else           -> "Faltan $daysLeft días"
            }
            binding.tvDaysLeft.text  = daysText
            binding.tvDaysLeft.setTextColor(urgencyColor)

            binding.root.setOnClickListener { onTaskClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

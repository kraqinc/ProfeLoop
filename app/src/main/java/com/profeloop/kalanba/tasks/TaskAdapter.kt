package com.profeloop.kalanba.tasks

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemTaskBinding
import com.profeloop.kalanba.models.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private var tasks: List<Task>,
    private val onClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    fun updateData(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.titulo
            binding.tvProfessor.text = "Por: ${task.profesorNombre}"

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val deadlineDate = Date(task.fechaLimite)
            binding.tvDeadline.text = "Límite: ${sdf.format(deadlineDate)}"

            val urgencyColor = getUrgencyColor(task.fechaLimite)
            binding.urgencyBar.setBackgroundColor(Color.parseColor(urgencyColor))

            binding.root.setOnClickListener { onClick(task) }
        }

        private fun getUrgencyColor(fechaLimite: Long): String {
            val now = System.currentTimeMillis()
            val diff = fechaLimite - now
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            return when {
                diff < 0 -> "#F44336"      // urgencyRed - past deadline
                days <= 2 -> "#FF9800"     // urgencyOrange - 2 days or less
                days <= 5 -> "#FFC107"     // urgencyYellow - 5 days or less
                else -> "#4CAF50"          // urgencyGreen - plenty of time
            }
        }
    }
}

package com.profeloop.kalanba.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemSubmissionBinding
import com.profeloop.kalanba.models.Submission
import com.profeloop.kalanba.utils.Constants

class SubmissionAdapter(
    private var submissions: List<Submission>,
    private val onGrade: (Submission, String) -> Unit
) : RecyclerView.Adapter<SubmissionAdapter.ViewHolder>() {

    fun updateData(newItems: List<Submission>) {
        submissions = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubmissionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(submissions[position])
    }

    override fun getItemCount() = submissions.size

    inner class ViewHolder(private val binding: ItemSubmissionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(submission: Submission) {
            binding.tvStudentName.text = submission.estudianteNombre
            binding.tvStatus.text = when (submission.estado) {
                Constants.ESTADO_CALIFICADO -> "Calificado: ${submission.nota}"
                Constants.ESTADO_REVISANDO -> "Revisando"
                else -> "Enviado"
            }
            binding.tvFileName.text = if (submission.archivoNombre.isNotEmpty())
                "Archivo: ${submission.archivoNombre}" else "Sin archivo adjunto"

            if (submission.nota.isNotEmpty()) {
                binding.etNota.setText(submission.nota)
            }

            binding.btnGrade.setOnClickListener {
                val nota = binding.etNota.text.toString().trim()
                if (nota.isNotEmpty()) {
                    onGrade(submission, nota)
                }
            }
        }
    }
}

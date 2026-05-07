package com.profeloop.kalanba.subjects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemSubjectBinding
import com.profeloop.kalanba.utils.Constants

class SubjectAdapter(
    private var items: List<Pair<String, String?>>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    fun updateData(newItems: List<Pair<String, String?>>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, String?>) {
            val (subject, teacher) = item
            val emoji = Constants.SUBJECT_EMOJIS[subject] ?: "📚"
            binding.tvEmoji.text = emoji
            binding.tvSubjectName.text = subject
            binding.tvTeacher.text = if (teacher != null) "Profesor: $teacher" else "Sin profesor asignado"
            binding.root.setOnClickListener { onClick(subject) }
        }
    }
}

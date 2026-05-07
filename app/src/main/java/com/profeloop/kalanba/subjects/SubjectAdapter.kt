package com.profeloop.kalanba.subjects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemSubjectBinding
import com.profeloop.kalanba.models.User

data class SubjectItem(
    val nombre: String,
    val profesorNombre: String,
    val tieneProfesor: Boolean,
    val emoji: String
)

class SubjectAdapter(
    private val grado: Int,
    private val currentUser: User?,
    private val onSubjectClick: (String) -> Unit
) : ListAdapter<SubjectItem, SubjectAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SubjectItem>() {
            override fun areItemsTheSame(a: SubjectItem, b: SubjectItem) = a.nombre == b.nombre
            override fun areContentsTheSame(a: SubjectItem, b: SubjectItem) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubjectItem) {
            binding.tvEmoji.text          = item.emoji
            binding.tvSubjectName.text    = item.nombre
            binding.tvProfesorName.text   = if (item.tieneProfesor) {
                "Prof. ${item.profesorNombre}"
            } else {
                "Sin profesor asignado"
            }
            binding.root.setOnClickListener { onSubjectClick(item.nombre) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

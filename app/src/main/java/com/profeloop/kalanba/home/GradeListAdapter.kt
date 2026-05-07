package com.profeloop.kalanba.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemGradeBinding
import com.profeloop.kalanba.databinding.ItemSectionHeaderBinding
import com.profeloop.kalanba.utils.Constants

class GradeListAdapter(
    private val items: List<Any>,
    private val onGradeClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_GRADE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_GRADE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemSectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = ItemGradeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            GradeViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as String)
            is GradeViewHolder -> holder.bind(items[position] as Int)
        }
    }

    override fun getItemCount() = items.size

    inner class HeaderViewHolder(private val binding: ItemSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.root.text = title
        }
    }

    inner class GradeViewHolder(private val binding: ItemGradeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(grade: Int) {
            binding.tvGradeNumber.text = grade.toString()
            binding.tvGradeLabel.text = "Grado $grade"
            val colorHex = Constants.GRADE_COLORS[grade - 1]
            binding.gradeCircle.background.setTint(Color.parseColor(colorHex))
            binding.root.setOnClickListener { onGradeClick(grade) }
        }
    }
}

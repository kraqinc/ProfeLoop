package com.profeloop.kalanba.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.ItemChatMessageBinding

class ChatAdapter(
    private val messages: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.isUser) {
                binding.layoutUser.visibility = View.VISIBLE
                binding.layoutAssistant.visibility = View.GONE
                binding.tvUserMessage.text = message.content
            } else {
                binding.layoutAssistant.visibility = View.VISIBLE
                binding.layoutUser.visibility = View.GONE
                binding.tvAssistantMessage.text = message.content
            }
        }
    }
}

package com.profeloop.kalanba.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.profeloop.kalanba.databinding.FragmentNotificationsBinding
import com.profeloop.kalanba.databinding.ItemNotificationBinding
import com.profeloop.kalanba.models.AppNotification
import com.profeloop.kalanba.utils.FirebaseUtils
import com.profeloop.kalanba.utils.gone
import com.profeloop.kalanba.utils.toFormattedDate
import com.profeloop.kalanba.utils.visible
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotifAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NotifAdapter { notif ->
            viewLifecycleOwner.lifecycleScope.launch {
                FirebaseUtils.markNotificationRead(notif.id)
            }
        }
        binding.rvNotifications.adapter = adapter
        loadNotifications()
        binding.swipeRefresh.setOnRefreshListener { loadNotifications() }
    }

    private fun loadNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visible()
            val uid   = FirebaseUtils.currentUid ?: return@launch
            val notifs = FirebaseUtils.getNotificationsForUser(uid)
            binding.progressBar.gone()
            binding.swipeRefresh.isRefreshing = false

            if (notifs.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvNotifications.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvNotifications.visible()
                adapter.submitList(notifs)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class NotifAdapter(
    private val onRead: (AppNotification) -> Unit
) : ListAdapter<AppNotification, NotifAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppNotification>() {
            override fun areItemsTheSame(a: AppNotification, b: AppNotification) = a.id == b.id
            override fun areContentsTheSame(a: AppNotification, b: AppNotification) = a == b
        }
    }

    inner class ViewHolder(private val b: ItemNotificationBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(n: AppNotification) {
            b.tvTitle.text   = n.titulo
            b.tvMessage.text = n.mensaje
            b.tvTime.text    = n.createdAt.toFormattedDate()
            b.root.alpha     = if (n.leida) 0.6f else 1.0f
            b.root.setOnClickListener { onRead(n) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}

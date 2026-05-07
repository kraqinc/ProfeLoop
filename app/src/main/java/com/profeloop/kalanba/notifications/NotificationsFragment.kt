package com.profeloop.kalanba.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.profeloop.kalanba.databinding.FragmentNotificationsBinding
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter(emptyList())
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val uid = FirebaseUtils.currentUid ?: return
        lifecycleScope.launch {
            val notifications = FirebaseUtils.getNotifications(uid)
                .sortedByDescending { it.timestamp }
            adapter.updateData(notifications)
            if (notifications.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

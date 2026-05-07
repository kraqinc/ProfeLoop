package com.profeloop.kalanba.tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.profeloop.kalanba.databinding.FragmentTaskListBinding
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private var grado = 1
    private var asignatura = ""
    private var periodo = 1

    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        grado = arguments?.getInt("extra_grado", 1) ?: 1
        asignatura = arguments?.getString("extra_asignatura", "") ?: ""
        periodo = arguments?.getInt("extra_periodo", 1) ?: 1

        adapter = TaskAdapter(emptyList()) { task ->
            val intent = Intent(requireContext(), TaskDetailActivity::class.java).apply {
                putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
                putExtra(TaskDetailActivity.EXTRA_GRADO, task.grado)
                putExtra(TaskDetailActivity.EXTRA_ASIGNATURA, task.asignatura)
            }
            startActivity(intent)
        }

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light
        )
        binding.swipeRefresh.setOnRefreshListener { loadTasks() }

        lifecycleScope.launch {
            val uid = FirebaseUtils.currentUid ?: return@launch
            val user = FirebaseUtils.getUserProfile(uid)
            if (user?.rol == Constants.ROL_PROFESOR) {
                binding.fabPublish.visibility = View.VISIBLE
            }
            loadTasks()
        }

        binding.fabPublish.setOnClickListener {
            val intent = Intent(requireContext(), PublishTaskActivity::class.java).apply {
                putExtra(PublishTaskActivity.EXTRA_GRADO, grado)
                putExtra(PublishTaskActivity.EXTRA_ASIGNATURA, asignatura)
            }
            startActivity(intent)
        }
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true
            val tasks = FirebaseUtils.getTasks(grado, asignatura, periodo)
            adapter.updateData(tasks)
            binding.swipeRefresh.isRefreshing = false
            if (tasks.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvTasks.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvTasks.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

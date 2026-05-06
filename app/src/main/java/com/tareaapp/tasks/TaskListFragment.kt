package com.tareaapp.tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tareaapp.MainActivity
import com.tareaapp.databinding.FragmentTaskListBinding
import com.tareaapp.models.User
import com.tareaapp.utils.Constants
import com.tareaapp.utils.FirebaseUtils
import com.tareaapp.utils.gone
import com.tareaapp.utils.visible
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter

    private var grado: Int      = 8
    private var asignatura: String = ""
    private var periodo: Int    = 1
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        grado      = arguments?.getInt(Constants.EXTRA_GRADO) ?: 8
        asignatura = arguments?.getString(Constants.EXTRA_ASIGNATURA) ?: ""
        periodo    = arguments?.getInt(Constants.EXTRA_PERIODO) ?: 1

        currentUser = (activity as? MainActivity)?.currentUser

        (activity as? MainActivity)?.supportActionBar?.title = "$asignatura — Período $periodo"

        setupAdapter()
        setupFab()
        loadTasks()

        binding.swipeRefresh.setOnRefreshListener { loadTasks() }
    }

    private fun setupAdapter() {
        adapter = TaskAdapter(
            isProfesor = currentUser?.esProfesor() == true,
            onTaskClick = { task ->
                val intent = Intent(requireContext(), TaskDetailActivity::class.java).apply {
                    putExtra(Constants.EXTRA_TAREA_ID, task.id)
                    putExtra(Constants.EXTRA_ASIGNATURA, asignatura)
                    putExtra(Constants.EXTRA_GRADO, grado)
                    putExtra(Constants.EXTRA_PERIODO, periodo)
                }
                startActivity(intent)
            }
        )
        binding.rvTasks.adapter = adapter
    }

    private fun setupFab() {
        if (currentUser?.esProfesor() == true) {
            binding.fabPublicarTarea.visible()
            binding.fabPublicarTarea.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    // Check if this teacher owns this subject-grade
                    val teacher = FirebaseUtils.getSubjectTeacher(grado, asignatura)
                    val myUid   = FirebaseUtils.currentUid
                    if (teacher != null && teacher.uid != myUid) {
                        com.tareaapp.utils.NotificationHelper.showLocalNotification(
                            requireContext(),
                            "Acceso denegado",
                            "Ya hay un/a profesor/a en esta asignatura"
                        )
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Ya hay un/a profesor/a en esta asignatura",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val intent = Intent(requireContext(), PublishTaskActivity::class.java).apply {
                            putExtra(Constants.EXTRA_GRADO,      grado)
                            putExtra(Constants.EXTRA_ASIGNATURA, asignatura)
                            putExtra(Constants.EXTRA_PERIODO,    periodo)
                        }
                        startActivity(intent)
                    }
                }
            }
        } else {
            binding.fabPublicarTarea.gone()
        }
    }

    private fun loadTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visible()
            val tasks = FirebaseUtils.getTasksForGradeSubjectPeriod(grado, asignatura, periodo)
            binding.progressBar.gone()
            binding.swipeRefresh.isRefreshing = false

            if (tasks.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvTasks.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvTasks.visible()
                adapter.submitList(tasks)
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

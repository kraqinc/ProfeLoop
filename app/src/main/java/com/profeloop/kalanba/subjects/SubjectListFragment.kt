package com.profeloop.kalanba.subjects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.profeloop.kalanba.MainActivity
import com.profeloop.kalanba.R
import com.profeloop.kalanba.databinding.FragmentSubjectListBinding
import com.profeloop.kalanba.models.Subject
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch

class SubjectListFragment : Fragment() {

    private var _binding: FragmentSubjectListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SubjectAdapter
    private var grado: Int = 8
    private var selectedPeriodo: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        grado = arguments?.getInt(Constants.EXTRA_GRADO) ?: 8

        (activity as? MainActivity)?.supportActionBar?.title = "Grado $grado°"

        setupPeriodoTabs()
        setupAdapter()
        loadSubjects()
    }

    private fun setupPeriodoTabs() {
        Constants.PERIODOS.forEach { periodo ->
            binding.tabsPeriodo.addTab(
                binding.tabsPeriodo.newTab().setText("Período $periodo")
            )
        }
        binding.tabsPeriodo.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedPeriodo = tab.position + 1
                loadSubjects()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupAdapter() {
        adapter = SubjectAdapter(
            grado          = grado,
            currentUser    = (activity as? MainActivity)?.currentUser,
            onSubjectClick = { asignatura ->
                findNavController().navigate(
                    R.id.action_subjectListFragment_to_taskListFragment,
                    bundleOf(
                        Constants.EXTRA_GRADO      to grado,
                        Constants.EXTRA_ASIGNATURA to asignatura,
                        Constants.EXTRA_PERIODO    to selectedPeriodo
                    )
                )
            }
        )
        binding.rvSubjects.adapter = adapter
    }

    private fun loadSubjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            val subjectItems = Subject.TODAS_LAS_ASIGNATURAS.map { nombre ->
                val teacher = FirebaseUtils.getSubjectTeacher(grado, nombre)
                SubjectItem(
                    nombre          = nombre,
                    profesorNombre  = teacher?.nombreCompleto() ?: "Sin asignar",
                    tieneProfesor   = teacher != null,
                    emoji           = Subject.emojiParaAsignatura(nombre)
                )
            }

            binding.progressBar.visibility = View.GONE
            adapter.submitList(subjectItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

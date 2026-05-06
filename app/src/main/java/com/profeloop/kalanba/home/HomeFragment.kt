package com.profeloop.kalanba.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.profeloop.kalanba.MainActivity
import com.profeloop.kalanba.R
import com.profeloop.kalanba.databinding.FragmentHomeBinding
import com.profeloop.kalanba.models.GradeSection
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GradeListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GradeListAdapter { grado ->
            findNavController().navigate(
                R.id.action_homeFragment_to_subjectListFragment,
                bundleOf(Constants.EXTRA_GRADO to grado)
            )
        }
        binding.rvGrades.adapter = adapter
        loadGrades()

        binding.swipeRefresh.setOnRefreshListener { loadGrades() }
    }

    private fun loadGrades() {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid  = FirebaseUtils.currentUid ?: return@launch
            val user = FirebaseUtils.getUserProfile(uid)
            binding.swipeRefresh.isRefreshing = false

            val mainActivity = activity as? MainActivity
            mainActivity?.currentUser = user

            val sections = if (user?.esProfesor() == true) {
                listOf(
                    GradeSection("Primaria",     Constants.GRADOS_PRIMARIA),
                    GradeSection("Bachillerato", Constants.GRADOS_BACHILLERATO)
                )
            } else {
                val grado = user?.grado ?: return@launch
                val nivel = user.nivel
                val sectionName = if (nivel == Constants.NIVEL_PRIMARIA) "Primaria" else "Bachillerato"
                listOf(GradeSection(sectionName, listOf(grado)))
            }

            val title = if (user?.esProfesor() == true) {
                "Hola, ${user.nombre} 👋"
            } else {
                "Hola, ${user?.nombre} 👋"
            }
            binding.tvWelcome.text = title
            adapter.submitSections(sections)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

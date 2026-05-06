package com.tareaapp.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tareaapp.auth.LoginActivity
import com.tareaapp.databinding.FragmentProfileBinding
import com.tareaapp.utils.FirebaseUtils
import com.tareaapp.utils.gone
import com.tareaapp.utils.visible
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()

        binding.btnCerrarSesion.setOnClickListener {
            FirebaseUtils.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visible()
            val uid  = FirebaseUtils.currentUid ?: return@launch
            val user = FirebaseUtils.getUserProfile(uid)
            binding.progressBar.gone()

            if (user != null) {
                binding.tvNombre.text     = user.nombreCompleto()
                binding.tvEmail.text      = user.email
                binding.tvRol.text        = if (user.esProfesor()) "Profesor/a" else "Estudiante"
                binding.tvGrado.text      = "Grado ${user.gradoStr()} · ${user.nivel.replaceFirstChar { it.uppercase() }}"
                val asignaturasStr = if (user.asignaturas.isEmpty()) {
                    "Sin asignaturas asignadas"
                } else {
                    user.asignaturas.joinToString(", ")
                }
                binding.tvAsignaturas.text = asignaturasStr

                val avatar = if (user.esProfesor()) "👨‍🏫" else "🎒"
                binding.tvAvatar.text = avatar
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

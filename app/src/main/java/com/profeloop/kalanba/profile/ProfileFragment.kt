package com.profeloop.kalanba.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.profeloop.kalanba.auth.LoginActivity
import com.profeloop.kalanba.ai.AiAssistantActivity
import com.profeloop.kalanba.databinding.FragmentProfileBinding
import com.profeloop.kalanba.utils.FirebaseUtils
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            FirebaseUtils.auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnAiAssistant.setOnClickListener {
            startActivity(Intent(requireContext(), AiAssistantActivity::class.java))
        }

        loadProfile()
    }

    private fun loadProfile() {
        val uid = FirebaseUtils.currentUid ?: return
        lifecycleScope.launch {
            val user = FirebaseUtils.getUserProfile(uid) ?: return@launch
            binding.tvName.text = user.nombre
            binding.tvEmail.text = user.email
            binding.tvRol.text = if (user.rol == "profesor") "Profesor" else "Estudiante"
            binding.tvNivel.text = if (user.nivel == "primaria") "Primaria" else "Bachillerato"
            binding.tvGrado.text = "Grado ${user.grado}"
            val initial = user.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            binding.tvAvatarInitial.text = initial
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

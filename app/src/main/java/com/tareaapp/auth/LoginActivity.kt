package com.tareaapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tareaapp.MainActivity
import com.tareaapp.databinding.ActivityLoginBinding
import com.tareaapp.utils.FirebaseUtils
import com.tareaapp.utils.gone
import com.tareaapp.utils.toast
import com.tareaapp.utils.visible
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Ingresa tu correo"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Ingresa tu contraseña"
                return@setOnClickListener
            }

            binding.tilEmail.error    = null
            binding.tilPassword.error = null
            doLogin(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin(email: String, password: String) {
        binding.progressBar.visible()
        binding.btnLogin.isEnabled = false

        FirebaseUtils.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                lifecycleScope.launch {
                    val uid   = FirebaseUtils.currentUid ?: return@launch
                    val token = FirebaseUtils.getFcmToken()
                    if (token != null) FirebaseUtils.updateFcmToken(uid, token)

                    binding.progressBar.gone()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.gone()
                binding.btnLogin.isEnabled = true
                val msg = when {
                    e.message?.contains("no user record") == true -> "No existe una cuenta con este correo"
                    e.message?.contains("password is invalid") == true -> "Contraseña incorrecta"
                    e.message?.contains("network") == true -> "Sin conexión a internet"
                    else -> "Error al iniciar sesión: ${e.message}"
                }
                toast(msg)
            }
    }
}

package com.profeloop.kalanba.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.profeloop.kalanba.MainActivity
import com.profeloop.kalanba.databinding.ActivityRegisterBinding
import com.profeloop.kalanba.models.User
import com.profeloop.kalanba.utils.Constants
import com.profeloop.kalanba.utils.FirebaseUtils
import com.profeloop.kalanba.utils.gone
import com.profeloop.kalanba.utils.toast
import com.profeloop.kalanba.utils.visible
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedRol    = Constants.ROL_ESTUDIANTE
    private var selectedNivel  = Constants.NIVEL_BACHILLERATO
    private var selectedGrado  = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRolSelector()
        setupNivelGradoSelectors()
        setupListeners()
    }

    private fun setupRolSelector() {
        binding.chipEstudiante.isChecked = true

        binding.chipGroupRol.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedRol = if (checkedIds.contains(binding.chipProfesor.id)) {
                Constants.ROL_PROFESOR
            } else {
                Constants.ROL_ESTUDIANTE
            }
        }
    }

    private fun setupNivelGradoSelectors() {
        val nivelesAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listOf("Primaria (1°-5°)", "Bachillerato (6°-9°)")
        )
        binding.actvNivel.setAdapter(nivelesAdapter)
        binding.actvNivel.setText("Bachillerato (6°-9°)", false)
        updateGradoAdapter()

        binding.actvNivel.setOnItemClickListener { _, _, position, _ ->
            selectedNivel = if (position == 0) Constants.NIVEL_PRIMARIA else Constants.NIVEL_BACHILLERATO
            updateGradoAdapter()
        }
    }

    private fun updateGradoAdapter() {
        val grados = if (selectedNivel == Constants.NIVEL_PRIMARIA) {
            Constants.GRADOS_PRIMARIA.map { "$it°" }
        } else {
            Constants.GRADOS_BACHILLERATO.map { "$it°" }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, grados)
        binding.actvGrado.setAdapter(adapter)
        binding.actvGrado.setText("", false)

        val defaultGrado = if (selectedNivel == Constants.NIVEL_PRIMARIA) 1 else 6
        selectedGrado = defaultGrado
        binding.actvGrado.setText("${defaultGrado}°", false)

        binding.actvGrado.setOnItemClickListener { _, _, position, _ ->
            selectedGrado = if (selectedNivel == Constants.NIVEL_PRIMARIA) {
                Constants.GRADOS_PRIMARIA[position]
            } else {
                Constants.GRADOS_BACHILLERATO[position]
            }
        }
    }

    private fun setupListeners() {
        binding.btnRegistrar.setOnClickListener { doRegister() }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun doRegister() {
        val nombre   = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm  = binding.etConfirmPassword.text.toString().trim()

        if (nombre.isEmpty())   { binding.tilNombre.error   = "Ingresa tu nombre";   return }
        if (apellido.isEmpty()) { binding.tilApellido.error = "Ingresa tu apellido"; return }
        if (email.isEmpty())    { binding.tilEmail.error    = "Ingresa tu correo";   return }
        if (password.length < 6) { binding.tilPassword.error = "Mínimo 6 caracteres"; return }
        if (password != confirm) { binding.tilConfirmPassword.error = "Las contraseñas no coinciden"; return }

        clearErrors()
        binding.progressBar.visible()
        binding.btnRegistrar.isEnabled = false

        FirebaseUtils.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                lifecycleScope.launch {
                    val token = FirebaseUtils.getFcmToken() ?: ""
                    val user  = User(
                        uid        = uid,
                        email      = email,
                        nombre     = nombre,
                        apellido   = apellido,
                        rol        = selectedRol,
                        nivel      = selectedNivel,
                        grado      = selectedGrado,
                        fcmToken   = token,
                        asignaturas = emptyList()
                    )
                    FirebaseUtils.saveUserProfile(user)
                    binding.progressBar.gone()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.gone()
                binding.btnRegistrar.isEnabled = true
                val msg = when {
                    e.message?.contains("email address is already in use") == true ->
                        "Ya existe una cuenta con este correo"
                    e.message?.contains("network") == true ->
                        "Sin conexión a internet"
                    else -> "Error al registrarse: ${e.message}"
                }
                toast(msg)
            }
    }

    private fun clearErrors() {
        binding.tilNombre.error          = null
        binding.tilApellido.error        = null
        binding.tilEmail.error           = null
        binding.tilPassword.error        = null
        binding.tilConfirmPassword.error = null
    }
}

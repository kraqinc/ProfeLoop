package com.profeloop.kalanba.ai

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.profeloop.kalanba.databinding.ActivityAiAssistantBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val role: String, val content: String)

class AiAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiAssistantBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    companion object {
        // La API key se pasa desde BuildConfig (definida en build.gradle como buildConfigField)
        const val GROQ_MODEL = "llama3-8b-8192"
        const val SYSTEM_PROMPT = """Eres un asistente de estudio para estudiantes de colegio. 
Tu rol es AYUDAR a entender los temas, NO dar las respuestas directas.
- Explica conceptos de forma clara y sencilla
- Haz preguntas que guíen al estudiante a pensar
- Da pistas, no soluciones completas
- Si el estudiante insiste en que le des la respuesta, recuérdale que aprender es más valioso
- Habla siempre en español, con un tono amigable y motivador
- Puedes ayudar con: Matemáticas, Física, Química, Biología, Inglés, Historia, Literatura y más"""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Asistente IA 🤖"

        adapter = ChatAdapter(messages)
        binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = adapter

        // Welcome message
        addMessage(ChatMessage("assistant",
            "¡Hola! Soy tu asistente de estudio 📚\n\nEstoy aquí para ayudarte a ENTENDER tus tareas, no para darte las respuestas. Cuéntame, ¿en qué tema necesitas ayuda?"))

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                binding.etMessage.setText("")
                sendMessage(text)
            }
        }
    }

    private fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun sendMessage(text: String) {
        addMessage(ChatMessage("user", text))
        binding.btnSend.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = callGroqApi(text)
                withContext(Dispatchers.Main) {
                    addMessage(ChatMessage("assistant", response))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnSend.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun callGroqApi(userMessage: String): String = withContext(Dispatchers.IO) {
        val apiKey = "gsk_WHSf1Jw8fgWePywahKOy" + "WGdyb3FY95q9PwIhT2hKwaiSM5yvX4GM"
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        // Build message history (last 10 messages for context)
        val historyArray = JSONArray()
        historyArray.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })
        val recentMessages = messages.takeLast(10)
        for (msg in recentMessages) {
            historyArray.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }

        val body = JSONObject().apply {
            put("model", GROQ_MODEL)
            put("messages", historyArray)
            put("max_tokens", 1024)
            put("temperature", 0.7)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

        val responseCode = conn.responseCode
        val stream = if (responseCode == 200) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().readText()

        if (responseCode != 200) {
            throw Exception("API error $responseCode: $response")
        }

        val json = JSONObject(response)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

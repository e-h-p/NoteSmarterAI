package com.example.notesmarterai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    // Gemini API key here
    private val apiKey = "YOUR_API_KEY_HERE"

    //  Data classes for Gemini API
    // Request Body data classes
    data class GeminiRequest(val contents: List<Content>, val generationConfig: GenerationConfig)
    data class Content(val parts: List<Part>)
    data class Part(val text: String)
    data class GenerationConfig(val maxOutputTokens: Int)

    // Response Body data classes
    data class GeminiResponse(val candidates: List<Candidate>)
    data class Candidate(val content: Content)


    private lateinit var inputMessage: EditText
    private lateinit var sendButton: Button
    private lateinit var outputMessage: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputMessage = findViewById(R.id.inputMessage)
        sendButton = findViewById(R.id.sendButton)
        outputMessage = findViewById(R.id.outputMessage)
        progressBar = findViewById(R.id.progressBar)

        sendButton.setOnClickListener {
            val userInput = inputMessage.text.toString()
            if (userInput.isNotBlank()) {
                progressBar.visibility = View.VISIBLE
                outputMessage.text = ""
                // Function name updated
                callGeminiAPI(userInput) { response ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        outputMessage.text = response
                    }
                }
            }
        }
    }

    // ✨ Step 3: Updated function for Gemini (instead of OpenAI)
    private fun callGeminiAPI(userText: String, callback: (String) -> Unit) {
        // New URL for Gemini
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val prompt = "Make a short note: Title, Desc, Category, Priority, Reminder from \"$userText\""

        // Create request data in Gemini’s format
        val requestData = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(maxOutputTokens = 100)
        )

        val json = Gson().toJson(requestData)
        val mediaType = "application/json".toMediaType()
        val body = json.toRequestBody(mediaType)

        // Gemini request does not require headers
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val resBody = res.body?.string()
                    if (!res.isSuccessful) {
                        callback("Error: ${res.code}\n${resBody}")
                        return
                    }

                    if (resBody != null) {
                        try {
                            // ✨ Step 4: Parse Gemini’s response
                            val jsonResponse = Gson().fromJson(resBody, GeminiResponse::class.java)
                            val result = jsonResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            callback(result?.trim() ?: "No content found in response")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            callback("Error parsing JSON: ${e.message}")
                        }
                    } else {
                        callback("No response from server")
                    }
                }
            }
        })
    }
}

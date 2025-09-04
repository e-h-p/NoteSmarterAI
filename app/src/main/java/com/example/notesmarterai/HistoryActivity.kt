package com.example.notesmarterai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyTextView: TextView = findViewById(R.id.historyTextView)
        historyTextView.text = readNotesFromFile()
    }

    private fun readNotesFromFile(): String {
        val filename = "notes_history.txt"
        val stringBuilder = StringBuilder()

        try {
            val fileInputStream = openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var text: String?
            while (bufferedReader.readLine().also { text = it } != null) {
                stringBuilder.append(text).append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "No history found."
        }

        return if (stringBuilder.isEmpty()) "No history found." else stringBuilder.toString()
    }
}
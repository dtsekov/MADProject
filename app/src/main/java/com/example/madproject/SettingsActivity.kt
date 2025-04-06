package com.example.madproject

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val editTextUserIdentifier: EditText = findViewById(R.id.editTextUserIdentifier)
        val editTextApiKey: EditText = findViewById(R.id.editTextApiKey)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        // Load existing user identifier if available
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val userIdentifier = sharedPreferences.getString("userIdentifier", "")
        val apiKey = sharedPreferences.getString("API_KEY", "")

        editTextUserIdentifier.setText(userIdentifier)
        editTextApiKey.setText(apiKey)

        buttonSave.setOnClickListener {
            val newUserIdentifier = editTextUserIdentifier.text.toString()
            val newApiKey = editTextApiKey.text.toString()
            if (newUserIdentifier.isNotBlank()) {
                sharedPreferences.edit().apply {
                    putString("userIdentifier", newUserIdentifier)
                    putString("API_KEY", newApiKey)
                    apply()
                }
                Toast.makeText(this, "User ID saved: $newUserIdentifier", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "User ID cannot be blank", Toast.LENGTH_LONG).show()
            }
        }













    }
}

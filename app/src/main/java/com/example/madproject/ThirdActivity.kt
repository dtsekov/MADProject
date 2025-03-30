package com.example.madproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import androidx.lifecycle.lifecycleScope
import com.example.madproject.room.AppDatabase
import com.example.madproject.room.CoordinatesEntity
import kotlinx.coroutines.launch


class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val latitude = intent.getStringExtra("latitude")
        val longitude = intent.getStringExtra("longitude")
        val altitude = intent.getStringExtra("altitude")
        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude, Altitude: $altitude")

        val buttonToSecond: Button = findViewById(R.id.mainButton)
        buttonToSecond.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }
        val textView: TextView = findViewById(R.id.textView)
       /* val test = readFileContentsDatabase()
        textView.text = test[0][0].replace("\"", "")
        for(list in test){
            val code = list[0]
            val latitude = list[10].toDouble()
            val longitude = list[11].toDouble()
            saveCoordinatesToDatabase(latitude, longitude, code)
    }*/


    textView.text = "Latitude: $latitude, Longitude: $longitude, Altitude: $altitude"


}


    private fun saveCoordinatesToDatabase(latitude: Double, longitude: Double, code: String) {
        val coordinates = CoordinatesEntity(
            code = code,
            latitude = latitude,
            longitude = longitude
        )
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.coordinatesDao().insert(coordinates)
        }
    }

    private fun readFileContentsDatabase(): List<List<String>> {
        val fileName = "fuentes202503.csv"
        return try {
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.map { it.split(";").map(String::trim) }.toList()
            }
        } catch (e: IOException) {
            listOf(listOf("Error reading file: ${e.message}"))
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed()
        finish()
    }
}
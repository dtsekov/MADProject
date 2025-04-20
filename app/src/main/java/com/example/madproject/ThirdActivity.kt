package com.example.madproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button

import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


import com.example.madproject.room2.AppDatabase2
import com.example.madproject.room2.CoordinatesEntity2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.EditText



class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"

    private lateinit var etTimestamp: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var etAltitude: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_third)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val buttonToSecond: Button = findViewById(R.id.mainButton)
        buttonToSecond.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }



        etTimestamp = findViewById(R.id.etTimestamp)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etAltitude = findViewById(R.id.etAltitude)



        // Obtener datos de la Intent
        val timestamp = intent.getStringExtra("timestamp")
        val latitude = intent.getStringExtra("latitude")
        val longitude = intent.getStringExtra("longitude")
        val altitude = intent.getStringExtra("altitude")

        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude, Altitude: $altitude")


        // Asignar datos a los EditText
        etTimestamp.setText(timestamp)
        etLatitude.setText(latitude)
        etLongitude.setText(longitude)
        etAltitude.setText(altitude)


        val deleteButton: Button = findViewById(R.id.buttonDelete)
        deleteButton.setOnClickListener {
            if (!timestamp.isNullOrEmpty()) {
                showDeleteConfirmationDialog(timestamp.toLong())
            }
        }
        // Botón para actualizar la coordenada
        val updateButton: Button = findViewById(R.id.buttonUpdate)
        updateButton.setOnClickListener {
            showUpdateConfirmationDialog()
        }





    }
    private fun showDeleteConfirmationDialog(timestamp: Long) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this coordinate?\n\n" +
                    "📍 Timestamp: $timestamp\n" +
                    "📍 Latitude: ${etLatitude.text}\n" +
                    "📍 Longitude: ${etLongitude.text}\n" +
                    "📍 Altitude: ${etAltitude.text}")
            .setPositiveButton("Delete") { _, _ ->
                deleteCoordinate(timestamp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteCoordinate(timestamp: Long) {
        val db = AppDatabase2.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.coordinatesDao().deleteWithTimestamp(timestamp)
            Log.d(TAG, "Coordinate with timestamp $timestamp deleted.")
            // Volver a SecondActivity después de borrar en el hilo principal
            withContext(Dispatchers.Main) {
                startActivity(Intent(this@ThirdActivity, SecondActivity::class.java))
                finish()
            }
        }
    }
    private fun showUpdateConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Update")
            .setMessage("Are you sure you want to update this coordinate?\n\n" +
                    "📍 Timestamp: ${etTimestamp.text}\n" +
                    "📍 Latitude: ${etLatitude.text}\n" +
                    "📍 Longitude: ${etLongitude.text}\n" +
                    "📍 Altitude: ${etAltitude.text}")
            .setPositiveButton("Update") { _, _ ->
                updateCoordinate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCoordinate() {
        val db = AppDatabase2.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val timestamp = etTimestamp.text.toString().toLong()
            val existingCoordinate = db.coordinatesDao().getCoordinateByTimestamp(timestamp)
            if (existingCoordinate != null) { // ✅ Verifica si existe antes de actualizar
                val updatedCoordinate = CoordinatesEntity2(
                    timestamp = timestamp, // Mantiene el mismo timestamp
                    latitude = etLatitude.text.toString().toDouble(),
                    longitude = etLongitude.text.toString().toDouble(),
                    altitude = etAltitude.text.toString().toDouble()
                )
                db.coordinatesDao().updateCoordinate(updatedCoordinate)
                Log.d(TAG, "✅ Coordinate updated: $updatedCoordinate")
            } else {
                Log.e(TAG, "⚠️ No coordinate found with timestamp $timestamp")
            }
            withContext(Dispatchers.Main) {
                startActivity(Intent(this@ThirdActivity, SecondActivity::class.java))
                finish()
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed()
        finish()
    }
}
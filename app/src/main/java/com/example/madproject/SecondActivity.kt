package com.example.madproject


import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import android.widget.ArrayAdapter
import android.widget.ListView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.lifecycle.lifecycleScope
import com.example.madproject.room2.AppDatabase2
import kotlinx.coroutines.launch



class SecondActivity : AppCompatActivity() {
    private val TAG = "btaSecondActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        Log.d(TAG, "onCreate: The activity is being created.");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonNext: Button = findViewById(R.id.mainButton)
        buttonNext.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
            finish()
        }
        val buttonNext2: Button = findViewById(R.id.secondPreviousButton)
        buttonNext2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable<Location>("location")
        if (location != null) {
            Log.i(TAG, "onCreate: Location["+location.altitude+"]["+location.latitude+"]["+location.longitude+"][")
        }

        /*
        val listView: ListView = findViewById(R.id.lvCoordinates)
        val headerView = layoutInflater.inflate(R.layout.listview_header, null)

        listView.addHeaderView(headerView, null, false)

        val adapter = CoordinatesAdapter(this, readFileContents())
        listView.adapter = adapter
        */
        val listView: ListView = findViewById(R.id.lvCoordinates)
        val headerView = layoutInflater.inflate(R.layout.listview_header, null)
        listView.addHeaderView(headerView, null, false)
        val db = AppDatabase2.getDatabase(this)
        // Obtener datos desde Room y crear el adaptador con los datos directamente
        lifecycleScope.launch {
            val dbCoordinates = db.coordinatesDao().getAll()
            val roomCoordinates = dbCoordinates.map {
                listOf(it.timestamp.toString(), it.latitude.toString(), it.longitude.toString(), it.altitude.toString())
            }
            Log.d(TAG, "Datos obtenidos de Room: $roomCoordinates")
            // Instanciar el adaptador con los datos de Room directamente
            val adapter = CoordinatesAdapter(this@SecondActivity, roomCoordinates)
            listView.adapter = adapter // Asignar el adaptador al ListView
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed()
        finish()
    }
    private class CoordinatesAdapter(context: Context, private val coordinatesList: List<List<String>>) :
        ArrayAdapter<List<String>>(context, R.layout.listview_item, coordinatesList) {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.listview_item, parent, false)
            val timestampTextView: TextView = view.findViewById(R.id.tvTimestamp)
            val latitudeTextView: TextView = view.findViewById(R.id.tvLatitude)
            val longitudeTextView: TextView = view.findViewById(R.id.tvLongitude)
            val altitudeTextView: TextView = view.findViewById(R.id.tvAltitude)
            val item = coordinatesList[position]
            try {
                timestampTextView.text = formatTimestamp(item[0].toLongOrNull() ?: 0L)
                latitudeTextView.text = formatCoordinate(item[1].toDoubleOrNull() ?: 0.0)
                longitudeTextView.text = formatCoordinate(item[2].toDoubleOrNull() ?: 0.0)
                altitudeTextView.text = formatCoordinate(item[3].toDoubleOrNull() ?: 0.0)
            } catch (e: Exception) {
                Log.e("CoordinatesAdapter", "Error convirtiendo valores: ${e.message}")
            }
            view.setOnClickListener {
                val intent = Intent(context, ThirdActivity::class.java).apply {
                    putExtra("timestamp", item[0])
                    putExtra("latitude", item[1])
                    putExtra("longitude", item[2])
                    putExtra("altitude", item[3])
                }
                context.startActivity(intent)
            }
            return view
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
        private fun formatCoordinate(value: Double): String {
            return String.format("%.4f", value)
        }
    }


    private fun readFileContents(): List<List<String>> {
        val fileName = "gps_coordinates.csv"
        return try {
            openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.map { it.split(";").map(String::trim) }.toList()
            }
        } catch (e: IOException) {
            listOf(listOf("Error reading file: ${e.message}"))
        }
    }




}
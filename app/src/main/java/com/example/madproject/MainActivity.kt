package com.example.madproject

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import android.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import java.io.File
import android.widget.Switch
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar








class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var latestLocation: Location
    private lateinit var locationSwitch: Switch


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: Starting main activity.")

        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            // If not, ask for it
            showUserIdentifierDialog()
        } else {
            // If yes, use it or show it
            Toast.makeText(this, "User ID: $userIdentifier", Toast.LENGTH_LONG).show()
        }


        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationSwitch = findViewById(R.id.locationSwitch)
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                locationSwitch.text = "Disable location"
                startLocationUpdates()
            } else {
                locationSwitch.text = "Enable location"
                stopLocationUpdates()
            }
        }

        val buttonOsm: Button = findViewById(R.id.osmButton)
        buttonOsm.setOnClickListener {

            if (latestLocation != null) {
                val intent = Intent(this, OpenStreetsMapActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable("location", latestLocation)
                intent.putExtra("locationBundle", bundle)
                startActivity(intent)
            } else {
                Log.e(TAG, "Location not set yet.")
            }
        }

        val buttonNext: Button = findViewById(R.id.mainButton)
        buttonNext.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("location", latestLocation)
            intent.putExtra("locationBundle", bundle)
            startActivity(intent)
        }
        val userIdentifierButton: Button = findViewById(R.id.user)
        userIdentifierButton.setOnClickListener {
            showUserIdentifierDialog()
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_map -> {
                    if (latestLocation != null) {
                        val intent = Intent(this, OpenStreetsMapActivity::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable("location", latestLocation)
                        intent.putExtra("locationBundle", bundle)
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Location not set yet.")
                    }
                    true
                }

                R.id.navigation_list -> {
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }



        private fun saveUserIdentifier(userIdentifier: String) {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userIdentifier", userIdentifier)
            apply()
        }
    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    private fun showUserIdentifierDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter User Identifier")
        val input = EditText(this)
        val userIdentifier = getUserIdentifier()
        if (userIdentifier != null) {
            input.setText(userIdentifier)
        }
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, which ->
            val userInput = input.text.toString()
            if (userInput.isNotBlank()) {
                saveUserIdentifier(userInput)
                Toast.makeText(this, "User ID saved: $userInput", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "User ID cannot be blank", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(this, "Thanks and goodbye!", Toast.LENGTH_LONG).show()
            dialog.cancel()
        }
        builder.show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        5f,
                        this
                    )
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionCode
            )
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }


    override fun onLocationChanged(location: Location) {
        latestLocation = location
        val textView: TextView = findViewById(R.id.textView)
        val locationText = "" + location.latitude + "" + location.longitude + "" + location.altitude
        textView.text = locationText
        saveCoordinatesToFile(
            location.latitude,
            location.longitude,
            location.altitude,
            System.currentTimeMillis()
        )
        val toastText =
            "New location: ${location.latitude}, ${location.longitude}, ${location.altitude}"
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
    }

    private fun saveCoordinatesToFile(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        timestamp: Long
    ) {
        val fileName = "gps_coordinates.csv"
        val file = File(filesDir, fileName)
        val formattedLatitude = String.format("%.4f", latitude)
        val formattedLongitude = String.format("%.4f", longitude)
        val formattedAltitude = String.format("%.2f", altitude)
        file.appendText("$timestamp;$formattedLatitude;$formattedLongitude;$formattedAltitude\n")
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
























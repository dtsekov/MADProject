package com.example.madproject

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.Manifest

import android.util.Log
import android.widget.Toast
import android.app.AlertDialog
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

import android.widget.Switch
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar


import androidx.lifecycle.lifecycleScope
import com.example.madproject.room2.AppDatabase
import com.example.madproject.room2.CoordinatesEntity
import kotlinx.coroutines.launch

import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.example.madproject.network.*

import android.widget.ImageView
import com.bumptech.glide.Glide












class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var latestLocation: Location? = null
    private lateinit var locationSwitch: Switch


    companion object {
        private const val RC_SIGN_IN = 123
    }


    private lateinit var weatherTextView: TextView
    private lateinit var weatherIcon: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: Starting main activity.")
        //launchSignInFlow()

        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            // If not, ask for it
            showUserIdentifierDialog()
        } else {
            // If yes, use it or show it
            val textView: TextView = findViewById(R.id.userNameTextView)
            textView.text = userIdentifier
            Toast.makeText(this, "User ID: $userIdentifier", Toast.LENGTH_LONG).show()
        }


        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        weatherIcon = findViewById(R.id.weatherIcon)
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

        weatherTextView = findViewById(R.id.weatherTextView)
        getWeatherForecast(40.38982289563083, -3.627826205293675)




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
            R.id.action_logout -> {
                
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

        saveCoordinatesToDatabase(location.latitude, location.longitude, location.altitude, System.currentTimeMillis())
        val toastText =
            "New location: ${location.latitude}, ${location.longitude}, ${location.altitude}"
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()

        getWeatherForecast(location.latitude, location.longitude)
    }
    private fun saveCoordinatesToDatabase(latitude: Double, longitude: Double, altitude: Double, timestamp: Long) {
        val coordinates = CoordinatesEntity(
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude
        )
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.coordinatesDao().insert(coordinates)
        }
    }
    private fun getWeatherForecast(lat: Double, lon: Double) {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No Internet connection")
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        val apiKey = getApiKey() ?: return

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherApiService::class.java)
        val call = service.getWeatherForecast(lat, lon, 1, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let { showWeatherInfo(it) }
                } else {
                    Log.e(TAG, "Error en la respuesta: ${response.code()}")
                    Toast.makeText(this@MainActivity, "Failed to fetch weather", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(TAG, "Error en la petición: ${t.message}")
                Toast.makeText(this@MainActivity, "Error fetching weather", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getApiKey(): String? {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("API_KEY", null)
    }
    private fun showWeatherInfo(weatherResponse: WeatherResponse) {
        val item = weatherResponse.list.firstOrNull()
        val weatherText = StringBuilder()
        if (item != null) {
            val tempCelsius = item.main.temp - 273.15
            val tempFormatted = String.format("%.1f", tempCelsius)
            val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@4x.png"

            weatherText.append("📍 ${item.name}\n")
            weatherText.append("🌡 Temp: $tempFormatted°C\n")
            weatherText.append("💨: ${item.weather[0].description}\n")
            weatherText.append("🌫 Humidity: ${item.main.humidity}%\n\n")

            weatherTextView.text = weatherText
            Glide.with(this).load(iconUrl).into(weatherIcon)
        }
    }

    override fun onResume() {
        super.onResume()
        val lat: Double
        val lon: Double
        if (latestLocation != null) {
            lat = latestLocation!!.latitude
            lon = latestLocation!!.longitude
            Log.d(TAG, "onResume: Reading last coordinates -> $lat, $lon")
        } else {
            lat = 40.38982289563083
            lon = -3.627826205293675
            Log.d(TAG, "onResume: Coordinates not read yet. Using default coordinates -> $lat, $lon")
        }
        getWeatherForecast(lat, lon)

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }










    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
























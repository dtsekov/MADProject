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
import android.widget.EditText






class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var latestLocation:Location
    private var userID = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        /*
        val latestLocation: Location? = with(locationManager) {
            val provider = getProvider(LocationManager.GPS_PROVIDER)
            getLastKnownLocation(provider.toString())
        */



        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check for location permissions
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionCode
            )
        } else {
            // The location is updated every 5000 milliseconds (or 5 seconds) and/or if the device moves more than 5 meters,
            // whichever happens first
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

        //val latestLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val buttonOsm: Button = findViewById(R.id.osmButton)
        buttonOsm.setOnClickListener {

            if (latestLocation != null) {
                val intent = Intent(this, OpenStreetsMapActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable("location", latestLocation)
                intent.putExtra("locationBundle", bundle)
                startActivity(intent)
            }else{
                Log.e(TAG, "Location not set yet.")
            }
        }

        val buttonNext: Button = findViewById(R.id.mainButton)
        buttonNext.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
          startActivity(intent)
        }
        val userIdentifierButton: Button = findViewById(R.id.user)
        userIdentifierButton.setOnClickListener {
            showUserIdentifierDialog()
            updateUserID()
        }


    }
    private fun updateUserID (){
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "${latestLocation.latitude}, ${latestLocation.longitude}, UserID: " + userID
    }

    private fun showUserIdentifierDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter User Identifier")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, which ->
            val userInput = input.text.toString()
            if (userInput.isNotBlank()) {
                Toast.makeText(this, "User ID saved: $userInput", Toast.LENGTH_LONG).show()
                userID = userInput

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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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



    override fun onLocationChanged(location: Location) {
        latestLocation = location;
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "${location.latitude}, ${location.longitude}, UserID: " + userID
        val toastText = "New location: ${location.latitude}, ${location.longitude}"
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

}






















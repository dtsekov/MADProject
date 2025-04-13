package com.example.madproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.location.Location
import android.util.Log

import androidx.activity.enableEdgeToEdge

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.Manifest
import android.content.Context
import android.graphics.Color
import org.osmdroid.views.overlay.Marker
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import org.osmdroid.views.overlay.Polyline
import androidx.lifecycle.lifecycleScope
import com.example.madproject.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.madproject.network.OSRMResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext




class OpenStreetsMapActivity : AppCompatActivity() {
    private var currentRoutePolyline: Polyline? = null
    private var lastSelectedMarker: Marker? = null
    private val TAG = "btaOpenStreetMapActivity"
    private lateinit var map: MapView
    private var currentLocation: Location? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting activity...");


        enableEdgeToEdge()
        setContentView(R.layout.activity_open_streets_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Configure the user agent before loading the configuration
        Configuration.getInstance().userAgentValue = "es.upm.btb.madproject"
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))

        val bundle = intent.getBundleExtra("locationBundle")
        currentLocation = bundle?.getParcelable("location")

        val startPoint = if (currentLocation != null) {
            Log.d(TAG, "onCreate: Location[${currentLocation!!.altitude}][${currentLocation!!.latitude}][${currentLocation!!.longitude}]")
            //GeoPoint(location.latitude, location.longitude)
            GeoPoint(40.42902050, -3.73263000)
        } else {
            Log.d(TAG, "onCreate: Location is null, using default coordinates")
            GeoPoint(40.389683644051864, -3.627825356970311)
        }

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(15.0)
        map.controller.setCenter(startPoint)

        // Add current location marker

        val marker = Marker(map).apply {

            position = startPoint
            title = "My current location"

            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            setOnMarkerClickListener { tappedMarker, mapView ->
                tappedMarker.showInfoWindow()
                mapView.controller.animateTo(tappedMarker.position)
                currentRoutePolyline?.let {
                    mapView.overlays.remove(it)
                    mapView.invalidate()
                }
                true
            }
        }
        marker.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_compass) as BitmapDrawable
        map.overlays.add(marker)




        loadDatabaseMarkers()





    }


    private fun getWalkingRoute(
            long1: Double,
            lat1: Double,
            long2: Double,
            lat2: Double,
            onRouteReady: (List<GeoPoint>) -> Unit
        ) {
            val client = OkHttpClient()
            val url = "https://router.project-osrm.org/route/v1/walking/" +
                    "$long1,$lat1;$long2,$lat2?overview=full&geometries=geojson"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = client.newCall(request).execute()
                    val json = response.body?.string()

                    if (response.isSuccessful && json != null) {
                        val moshi = Moshi.Builder().build()
                        val adapter = moshi.adapter(OSRMResponse::class.java)

                        val osrmResponse = adapter.fromJson(json)
                        val coords = osrmResponse?.routes?.firstOrNull()?.geometry?.coordinates

                        val geoPoints = coords?.map { coord ->
                            GeoPoint(coord[1], coord[0]) // lat, lon
                        } ?: emptyList()

                        withContext(Dispatchers.Main) {
                            onRouteReady(geoPoints)
                        }
                    } else {
                        println("HTTP Error: ${response.code}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }



    private fun loadDatabaseMarkers() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val dbCoordinates = db.coordinatesDao().getAll()
            val roomGeoPoints = dbCoordinates.map {
                GeoPoint(it.latitude, it.longitude)
            }
            Log.d(TAG, "Coordenadas obtenidas de Room: $roomGeoPoints")
            withContext(Dispatchers.Main) {
                addDatabaseMarkers(map, roomGeoPoints, this@OpenStreetsMapActivity)
            }
        }
    }
    private fun addDatabaseMarkers(map: MapView, coords: List<GeoPoint>, context: Context) {
        for (geoPoint in coords) {
            val marker = Marker(map).apply {

                position = geoPoint
                title = "Drinking Fountain"
                icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation) as BitmapDrawable
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                setOnMarkerClickListener { tappedMarker, mapView ->
                    val selectedPoint = tappedMarker.position
                    tappedMarker.showInfoWindow()
                    mapView.controller.animateTo(tappedMarker.position)

                    // Prevent same marker from redrawing
                    if (tappedMarker == lastSelectedMarker) return@setOnMarkerClickListener true

                    lastSelectedMarker = tappedMarker

                    // Clear previous polyline if it exists
                    currentRoutePolyline?.let {
                        mapView.overlays.remove(it)
                        mapView.invalidate()
                    }
                    //val startLat = currentLocation!!.latitude
                    //val startLon = currentLocation!!.longitude

                    val startLat = 40.42902050
                    val startLon = -3.73263000
                    val endLat = selectedPoint.latitude
                    val endLon = selectedPoint.longitude

                    getWalkingRoute(startLon, startLat, endLon, endLat) { geoPoints ->
                        val polyline = Polyline().apply {
                            setPoints(geoPoints)
                            color = Color.BLUE
                            width = 8f
                        }
                        currentRoutePolyline = polyline
                        mapView.overlays.add(polyline)
                        mapView.invalidate()
                    }

                    true
                }
            }
            map.overlays.add(marker)
        }
    }
}

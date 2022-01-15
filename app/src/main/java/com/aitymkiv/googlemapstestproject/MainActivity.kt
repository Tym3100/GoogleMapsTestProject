package com.aitymkiv.googlemapstestproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aitymkiv.googlemapstestproject.model.ApiClient
import com.aitymkiv.googlemapstestproject.model.Coordinate
import com.aitymkiv.googlemapstestproject.model.LineCoordinate
import com.aitymkiv.googlemapstestproject.model.MainJsonObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener {
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"
    private val COLOR_RED_ARGB = -0x1550000

    private var map: GoogleMap? = null
    private var lastKnownLocation: Location? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var locationPermissionGranted = false
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    private var mainJsonObject: MainJsonObject? = null

    private var linesCoordinate: ArrayList<LineCoordinate> = arrayListOf()
    private var pointsCoordinate: ArrayList<Coordinate> = arrayListOf()

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        setContentView(R.layout.activity_main)
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this)
        button = findViewById(R.id.updateButton)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                updateGoogleMap()
                Toast.makeText(this@MainActivity, "Данные обновились", Toast.LENGTH_LONG).show()
            }
        })
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun stylePolyline(polyline: Polyline) {
        polyline.color = COLOR_RED_ARGB
    }

    private fun updateGoogleMap() {
        map?.clear()
//        var polyline = map?.let { map1 ->
//            map1.clear()
//            map1.addPolyline(
//                PolylineOptions().addAll(
//                    linesCoordinate.map { item ->
//                        item.lat?.let { lat ->
//                            item.lon?.let { lon ->
//                                LatLng(lat, lon)
//                            }
//                        }
//                    }
//                )
//            )

        linesCoordinate.forEach {
            Log.d("Check", "NEW LINE" )
            map?.let { map1 ->
                map1.addPolyline(
                    PolylineOptions().addAll(it.coordinates.map { item ->
                        item.lat?.let { lat ->
                            item.lon?.let { lon ->
                                LatLng(lat, lon)
                            }
                        }
                    })
                )
            }
        }


        pointsCoordinate.forEach {
            it.lat?.let { it1 -> it.lon?.let { it2 -> LatLng(it1, it2) } }?.let { it2 ->
                CircleOptions()
                    .center(it2)
                    .radius(1.0)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE)
            }?.let { it3 ->
                map?.addCircle(
                    it3
                )
            }
        }

        linesCoordinate.forEach {
            it.coordinates.forEach{
                var polyline = map?.addPolyline(PolylineOptions().add(
                    LatLng(it.lat!!, it.lon!!)
                ))
                if (polyline != null) {
                    stylePolyline(polyline)
                }
            }
        }
    }

    private fun mapping() {
        if (!linesCoordinate.isNullOrEmpty()) {
            linesCoordinate.clear()
        }
        val lines = mainJsonObject?.lines
        val points = mainJsonObject?.points
        lines?.forEach {
            var coord: ArrayList<Coordinate> = arrayListOf()
            it.pointsGeometry?.coordinates?.forEach{
                coord.add(Coordinate(it.get(0), it.get(1), it.get(2)))
            }
            linesCoordinate.add(LineCoordinate(coord))
            Log.d("dede", it.pointsGeometry?.coordinates.toString())
        }
        Log.d("dede", linesCoordinate.toString())
        points?.forEach {
            pointsCoordinate.add(
                Coordinate(
                    it.pointsGeometry?.coordinates?.get(0),
                    it.pointsGeometry?.coordinates?.get(1),
                    it.pointsGeometry?.coordinates?.get(2)
                )
            )
        }
        linesCoordinate.forEach {
            Log.d("TagCheckLines", it.toString())
        }
        pointsCoordinate.forEach {
            Log.d("TagCheckPoints", it.toString())
        }
    }

    private fun getData() {
        ApiClient
            .getClient
            .getPoint()
            .enqueue(object : Callback<MainJsonObject> {
                override fun onResponse(
                    call: Call<MainJsonObject>?,
                    response: Response<MainJsonObject>?
                ) {
                    if (response?.isSuccessful == true) {
                        response.body()?.let {
                            mainJsonObject = it
                            mapping()
                            updateGoogleMap()
                        }
                    }
                    Log.d("TAG", mainJsonObject.toString())
                }

                override fun onFailure(call: Call<MainJsonObject>?, t: Throwable?) {
                    Log.e("TAG", t.toString())
                    Toast.makeText(
                        this@MainActivity,
                        "Не удалось получить данные: " + t.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }

            })
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.moveCamera(
            CameraUpdateFactory
                .newLatLngZoom(LatLng(-23.684, 133.903), 4f)
        )


        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()

        googleMap.setOnPolylineClickListener(this)
        googleMap.setOnPolygonClickListener(this)

        getData()
    }

    override fun onPolylineClick(polyline: Polyline) {
        TODO("Not yet implemented")
    }

    override fun onPolygonClick(p0: Polygon) {
        TODO("Not yet implemented")
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}
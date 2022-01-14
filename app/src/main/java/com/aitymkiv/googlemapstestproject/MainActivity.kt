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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Transformations
import com.aitymkiv.googlemapstestproject.model.ApiClient
import com.aitymkiv.googlemapstestproject.model.Coordinate
import com.aitymkiv.googlemapstestproject.model.MainJsonObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    GoogleMap.OnPolygonClickListener {
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"
    private var map: GoogleMap? = null
    private var lastKnownLocation: Location? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var locationPermissionGranted = false
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)
    private var mainJsonObject: MainJsonObject? = null

    private var linesCoordinate: ArrayList<Coordinate> = arrayListOf()
    private var pointsCoordinate: ArrayList<Coordinate> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        setContentView(R.layout.activity_main)
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun updateGoogleMap() {
        // не понимаю, стоппппппппппппппппп

        Log.e("dede", "e" + linesCoordinate[3].lat + "w" + linesCoordinate[3].lon)

//        map?.addPolyline(PolylineOptions() // вопрос, а latitude может быть положительной? ага
//
//            .clickable(true)
//            .add(
//                LatLng(linesCoordinate.get(0).lat!!, linesCoordinate.get(0).lon!!),
//                LatLng(linesCoordinate.get(1).lat!!, linesCoordinate.get(1).lon!!),
//                LatLng(linesCoordinate.get(2).lat!!, linesCoordinate.get(2).lon!!),
//                LatLng(linesCoordinate.get(3).lat!!, linesCoordinate.get(3).lon!!),
//                LatLng(linesCoordinate.get(4).lat!!, linesCoordinate.get(4).lon!!),
//                LatLng(linesCoordinate.get(5).lat!!, linesCoordinate.get(5).lon!!),
//                LatLng(linesCoordinate.get(6).lat!!, linesCoordinate.get(6).lon!!),
//                LatLng(linesCoordinate.get(7).lat!!, linesCoordinate.get(7).lon!!),
//                LatLng(linesCoordinate.get(8).lat!!, linesCoordinate.get(8).lon!!),
//                LatLng(-34.747, 145.592),
//                LatLng(-34.364, 147.891),
//                LatLng(-33.501, 150.217),
//                LatLng(-32.306, 149.248),
//                LatLng(-32.491, 147.309)))  // работает ничего не понимаю. Работает

//        val polyline1 = map?.addPolyline(PolylineOptions()
//            .clickable(true)
//            .add(
//                LatLng(linesCoordinate.get(0).lat!!, linesCoordinate.get(0).lon!!),
//                LatLng(linesCoordinate.get(1).lat!!, linesCoordinate.get(1).lon!!),
//                LatLng(linesCoordinate.get(2).lat!!, linesCoordinate.get(2).lon!!),
//                LatLng(linesCoordinate.get(3).lat!!, linesCoordinate.get(3).lon!!),
//                LatLng(linesCoordinate.get(4).lat!!, linesCoordinate.get(4).lon!!),
//                LatLng(linesCoordinate.get(5).lat!!, linesCoordinate.get(5).lon!!),
//                LatLng(linesCoordinate.get(6).lat!!, linesCoordinate.get(6).lon!!),
//                LatLng(linesCoordinate.get(7).lat!!, linesCoordinate.get(7).lon!!),
//                LatLng(linesCoordinate.get(8).lat!!, linesCoordinate.get(8).lon!!))) // а это - нет

        map?.let{ map1 ->
            map1.clear() // эмм polyline оно же е используетс. Не, карта прорисовывается в map1.addPolyling дак скопируй снизу( да нет я удалил оттуда
            map1.addPolyline(
                PolylineOptions().addAll(
                    linesCoordinate.map { item ->
                        item.lat?.let { lat ->
                            item.lon?.let { lon ->
                                LatLng(lat, lon)
                            }
                        }
                    }// уаххахаха я нашел ошибку, создавал лист при кажой итерации) фак ничего, а бля точностопяЯ
                )
            )
        }
    }

    private fun mapping() {
        if (!linesCoordinate.isNullOrEmpty()) {
            linesCoordinate.clear()
        }
        val lines = mainJsonObject?.lines
        val points = mainJsonObject?.points
        lines?.forEach {

            it.pointsGeometry?.coordinates?.forEach {
                linesCoordinate.add(Coordinate(it.get(0), it.get(1), it.get(2)))
            }
        }
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
                        "Нет подключения к интернету ",
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
        val circle = googleMap.addCircle(
            CircleOptions().center(LatLng(-33.8, 151.2))
                .radius(10000.0)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE)
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

//    @SuppressLint("MissingPermission")
//    private fun showCurrentPlace() {
//        if (map == null) {
//            return
//        }
//        if (locationPermissionGranted) {
//            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
//            val request = FindCurrentPlaceRequest.newInstance(placeFields)
//            val placeResult = placesClient.findCurrentPlace(request)
//            placeResult.addOnCompleteListener { task ->
//                if (task.isSuccessful && task.result != null) {
//                    val likelyPlaces = task.result
//                    val count =
//                        if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
//                            likelyPlaces.placeLikelihoods.size
//                        } else {
//                            M_MAX_ENTRIES
//                        }
//                    var i = 0
//                    likelyPlaceNames = arrayOfNulls(count)
//                    likelyPlaceAddresses = arrayOfNulls(count)
//                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
//                    likelyPlaceLatLngs = arrayOfNulls(count)
//                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
//                        likelyPlaceNames[i] = placeLikelihood.place.name
//                        likelyPlaceAddresses[i] = placeLikelihood.place.address
//                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
//                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
//                        i++
//                        if (i > count - 1) {
//                            break
//                        }
//                    }
//
//                    openPlacesDialog()
//                } else {
//                    Log.e(TAG, "Exception: %s", task.exception)
//                }
//            }
//        } else {
//            Log.i(TAG, "The user did not grant location permission.")
//            map?.addMarker(
//                MarkerOptions()
//                    .title(getString(R.string.default_info_title))
//                    .position(defaultLocation)
//                    .snippet(getString(R.string.default_info_snippet))
//            )
//            getLocationPermission()
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }


    private fun openPlacesDialog() {
        val listener =
            DialogInterface.OnClickListener { dialog, which ->
                val markerLatLng = likelyPlaceLatLngs[which]
                var markerSnippet = likelyPlaceAddresses[which]
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = """
                $markerSnippet
                ${likelyPlaceAttributions[which]}
                """.trimIndent()
                }

                if (markerLatLng == null) {
                    return@OnClickListener
                }

                map?.addMarker(
                    MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet)
                )

                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        markerLatLng,
                        DEFAULT_ZOOM.toFloat()
                    )
                )
            }

        AlertDialog.Builder(this)
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
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

        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        private const val M_MAX_ENTRIES = 5
    }
}
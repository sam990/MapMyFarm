package com.mapmyfarm.mapmyfarm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.SphericalUtil
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class TraceFarm : FragmentActivity(), OnMapReadyCallback {

    companion object {
        private const val UPDATE_INTERVAL = 100L
        private const val FASTEST_UPDATE_INTERVAL = 100
        private const val TAG = "TraceFarm"
        private const val DELAY = 1
        const val POLYGON_PADDING = 200
        var imageSize = 300
        private const val distanceBetweenPoints = 1.0
        const val ACRE_CONVERSION_RATE = 247.10538146717
    }

    private var mMap: GoogleMap? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var mLocationRequest: LocationRequest
    private var lastMarker: Marker? = null
    private var lastLocation: Location? = null
    private val REQUEST_CHECK_SETTINGS = 2
    private var isTracing = false
    var polygon: Polygon? = null
    private var mLocationCallback: LocationCallback? = null
    private var currentCounter = 0
    var area = 0.0
    private var traceConfirm: FloatingActionButton? = null
    private var traceReset: FloatingActionButton? = null
    private var traceDismiss: FloatingActionButton? = null
    private var progressBar: ProgressBar? = null
    private var locationUpdateEnabled = false
    private lateinit var pointsList: ArrayList<LatLng>
    @Volatile
    private var location : String = "unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trace_farm)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = (supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
        initialiseButtons()
        manageLocationSetting()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    protected fun manageLocationSetting() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this)

        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequest)
        task.addOnSuccessListener(this) {
            initialiseLocationUpdate()
            startLocationUpdate()
        }
        task.addOnFailureListener(this) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@TraceFarm,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun initialiseLocationUpdate() {
        mFusedLocationProviderClient = getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // do work here
                locationChanged(locationResult.getLastLocation())
            }
        }
    }

    private fun startLocationUpdate() {
        locationUpdateEnabled = true
        mFusedLocationProviderClient?.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun stopLocationUpdate() {
        locationUpdateEnabled = false
        if (mFusedLocationProviderClient != null && mLocationCallback != null) {
            mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
        }
    }

    private fun locationChanged(location: Location) {
        if (location == null) {
            showError("Unable to get location")
        }
        assert(location != null)
        val latLng = LatLng(location.latitude, location.longitude)
        if (isTracing) {
            if (++currentCounter == DELAY) {
                currentCounter = 0
                if ((lastLocation?.distanceTo(location) ?: 0.0F) >= distanceBetweenPoints) {
                    lastLocation = location
                    val list: ArrayList<LatLng> = ArrayList(polygon!!.points)
                    list.add(list.size - 1, latLng)
                    polygon?.points = list
                    //                    polygon = mMap.addPolygon(new PolygonOptions().addAll(list));
                }
            }
        }
        lastLocation = if (lastMarker == null) {
            updateMarker(location)
            mMap!!.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng, 18f
                )
            )
            location
        } else {
            updateMarker(location)
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            location
        }
    }

    private fun updateMarker(currentLocation: Location) {
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        if (lastMarker == null) lastMarker = mMap!!.addMarker(
            MarkerOptions().icon(
                BitmapDescriptorFactory.fromBitmap(
                    resizeMapIcons(
                        "map_marker",
                        20,
                        20
                    )
                )
            ).position(latLng)
        ) else {
            lastMarker?.setPosition(latLng)
        }
    }

    fun resizeMapIcons(iconName: String?, width: Int, height: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(
            resources,
            resources.getIdentifier(iconName, "drawable", packageName)
        )
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                initialiseLocationUpdate()
                startLocationUpdate()
            } else {
                showError("Please enable location")
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(
            findViewById(R.id.trace_reset),
            message,
            Snackbar.LENGTH_LONG
        )
            .setAction("Action", null)
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    if (event == DISMISS_EVENT_TIMEOUT) {
                        finish()
                    }
                }
            })
            .show()
    }

    public override fun onStop() {
        super.onStop()
        if (mFusedLocationProviderClient != null && mLocationCallback != null) {
            mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
        }
    }

    private fun initialiseButtons() {
        traceConfirm = findViewById(R.id.trace_confirm)
        traceConfirm?.setOnClickListener {
            run {
                if (!isTracing) {
                    polygon = mMap!!.addPolygon(
                        PolygonOptions()
                            .add(
                                lastMarker?.position,
                                lastMarker?.position
                            ).strokeColor(R.color.polygonStrokeColor)
                    )

//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-27.457, 153.040)));
                    isTracing = true
                    traceConfirm?.setImageResource(R.drawable.ic_send)
                    Snackbar.make(
                        findViewById(R.id.trace_confirm),
                        "Move along perimeter, press same button to finish", Snackbar.LENGTH_LONG
                    )
                        .setAction("Action", null)
                        .show()
                } else {
                    if (polygon!!.points.size < 4) {
                        Snackbar.make(
                            findViewById(R.id.trace_confirm),
                            "Not enough points", Snackbar.LENGTH_LONG
                        )
                            .setAction("Action", null)
                            .show()
                    } else {
                        stopLocationUpdate()
                        runOnUiThread { progressBar!!.visibility = View.VISIBLE }
                        pointsList = ArrayList(polygon!!.points)
                        getLocationAndCalculateArea()
                    }
                }
            }
        }
        traceDismiss = findViewById(R.id.trace_dismiss)
        traceDismiss?.setOnClickListener { finish() }
            traceReset = findViewById(R.id.trace_reset)
        traceReset?.setOnClickListener {
            if (isTracing) {
                isTracing = false
                traceConfirm?.setImageResource(android.R.drawable.ic_input_add)
            }
            if (polygon != null) {
                polygon?.remove()
                polygon = null
            }
            currentCounter = 0
            if (!locationUpdateEnabled) {
                startLocationUpdate()
            }
        }

            //initialising progress bar
        progressBar = findViewById(R.id.progress_confirm)
    }

    private fun hideTraceButtons() {
        traceConfirm!!.hide()
        traceReset!!.hide()
        traceDismiss!!.hide()
    }

    private fun getLocationAndCalculateArea() {
            Thread(Runnable {
                val geocoder = Geocoder(this@TraceFarm, Locale.getDefault())
                val addresses: List<Address>
                try {
                    val loc = pointsList[0]
                    addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    location = addresses[0].locality ?: "unknown"
                } catch (e: NullPointerException) {
                    location = "unknown"
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                area = SphericalUtil.computeArea(pointsList) * ACRE_CONVERSION_RATE
                runOnUiThread { tracingComplete() }
            }).start()
        }

    private fun showTraceButtons() {
        traceConfirm!!.show()
        traceReset!!.show()
        traceDismiss!!.show()
    }

    private fun tracingComplete() {
        if (lastMarker != null)
            lastMarker?.remove()
        lastMarker = null
        val myIntent = Intent(this, FarmInput::class.java)
        myIntent.putExtra("POINTS_LIST", pointsList)
        myIntent.putExtra("LOCATION", location)
        myIntent.putExtra("AREA", area)
        startActivity(myIntent)
        finish()
    }

}
package com.mapmyfarm.mapmyfarm


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mapmyfarm.mapmyfarm.FarmsDataStore.farmMap


class FarmDisplay : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private lateinit var mMap: GoogleMap
    private val POLYGON_PADDING= 200
    lateinit var farm: Farm
    var farmID = ""

    lateinit var deleteButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farm_display)
        title = "Farm View"
        farmID = intent.getStringExtra("FARM_ID") ?: ""


        farmMap[farmID]?.let {
            farm = it
        } ?: run {
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            finish()
        }

        val mapFragment = (supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)
        initialiseButtons()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        val polygon: Polygon = mMap.addPolygon(
            PolygonOptions().addAll(farm.coordinates)
                .strokeColor(R.color.polygonStrokeColor)
        )
        val latLngBounds = getPolygonLatLngBounds(polygon.points)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, POLYGON_PADDING))
    }

    private fun getPolygonLatLngBounds(polygon: List<LatLng?>): LatLngBounds? {
        val centerBuilder = LatLngBounds.builder()
        for (point in polygon) {
            centerBuilder.include(point)
        }
        return centerBuilder.build()
    }

    private fun initialiseButtons() {
        deleteButton = findViewById(R.id.delete_area)
        deleteButton.setOnClickListener {
            Snackbar.make(
                findViewById(R.id.delete_area),
                "Confirm delete", Snackbar.LENGTH_LONG
            )
                .setAction("Delete" ){
                        deleteOperation()
                }
                .show()
        }
    }

    private fun deleteOperation() {
        FarmsDataStore.deleteFarm(farm.id) {
            if (it) {
                runOnUiThread {
                    this.setResult(FarmEdit.DELETED)
                    finish()
                }
            } else {
                runOnUiThread {
                    Snackbar.make(
                            findViewById(R.id.delete_area),
                            "Unable to delete. Try Later", Snackbar.LENGTH_LONG
                        )
                        .show()
                }
            }
        }
    }


}

package com.mapmyfarm.mapmyfarm


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eyalbira.loadingdots.LoadingDots
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.mapmyfarm.mapmyfarm.FarmsDataStore.farmMap


class FarmDisplay : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private lateinit var mMap: GoogleMap
    private val POLYGON_PADDING= 200
    @Volatile lateinit var farm: Farm
    var farmID = ""

    lateinit var deleteButton: FloatingActionButton
    lateinit var updateButton: FloatingActionButton

    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var sheetSave: MaterialButton
    lateinit var sheetCancel: MaterialButton
    lateinit var sheetArea: TextInputLayout
    lateinit var sheetLocale: TextInputLayout
    lateinit var sheetLandType: TextInputLayout
    lateinit var saveLoading: LoadingDots


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
        initialiseBottomSheet()
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
        deleteButton = findViewById(R.id.delete_farm)
        deleteButton.setOnClickListener {
            Snackbar.make(
                findViewById(R.id.delete_farm),
                "Confirm delete", Snackbar.LENGTH_LONG
            )
                .setAction("Delete" ){
                        deleteOperation()
                }
                .show()
        }

        updateButton = findViewById(R.id.edit_farm)
        updateButton.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun deleteOperation() {
        FarmsDataStore.deleteFarm(farm.id) {
            if (it) {
                runOnUiThread {
                    this.setResult(HarvestEdit.DELETED)
                    finish()
                }
            } else {
                runOnUiThread {
                    Snackbar.make(
                            findViewById(R.id.delete_farm),
                            "Unable to delete. Try Later", Snackbar.LENGTH_LONG
                        )
                        .show()
                }
            }
        }
    }

    fun initialiseBottomSheet() {
        val bottomSheet = findViewById<View>(R.id.trace_farm_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.isDraggable = false

        sheetArea = findViewById(R.id.farm_area_input)
        sheetLocale = findViewById(R.id.farm_locale_input)
        sheetLandType = findViewById(R.id.farm_landtype_input)

        // save and cancel
        sheetSave = findViewById(R.id.bottom_sheet_save)
        sheetCancel = findViewById(R.id.bottom_sheet_cancel)
        saveLoading = findViewById(R.id.bottom_sheet_loadingdot)

        // sheet cancel
        sheetCancel.setOnClickListener {
            closeBottomSheet()
        }

        sheetSave.setOnClickListener {
            sheetSave.post {
                sheetSave.text = ""
                sheetSave.isEnabled = false
            }
            saveLoading.post { saveLoading.visibility = View.VISIBLE }
            updateFarm()
        }
    }

    private fun showBottomSheet() {
        sheetArea.editText?.setText(farm.area.toString())
        sheetLocale.editText?.setText(farm.locale)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    private fun closeBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    // update the farm
    private fun updateFarm() {

        val finalArea = sheetArea.editText?.text.toString().toDouble()
        val finalLocale = sheetLocale.editText?.text.toString()
        val landType = sheetLandType.editText?.text.toString()

        FarmsDataStore.updateFarm(farm.id, farm.farmID, finalArea, finalLocale,  landType) {
            if (it) {
                saveComplete()
            } else {
                saveError()
            }
        }
    }

    private fun saveError() {
        saveLoading.post { saveLoading.visibility = View.GONE }
        sheetSave.post {
            sheetSave.text = getString(R.string.save)
            sheetSave.isEnabled = true
        }
        runOnUiThread {
            Toast.makeText(applicationContext, "Error saving farm. Please check connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveComplete() {
        saveLoading.post { saveLoading.visibility = View.GONE }
        sheetSave.post { sheetSave.text = getString(R.string.save) }
        farm = farmMap[farmID] ?: Farm()
        runOnUiThread {
            closeBottomSheet()
            Snackbar.make(
                    findViewById(R.id.delete_farm),
                    "Updated Farm", Snackbar.LENGTH_SHORT
                )
                .show()
        }

    }

}

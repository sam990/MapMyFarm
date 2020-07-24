package com.mapmyfarm.mapmyfarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mapmyfarm.mapmyfarm.FarmsDataStore.harvestMap
import com.mapmyfarm.mapmyfarm.FarmsDataStore.updateFarm
import com.mapmyfarm.mapmyfarm.FarmsDataStore.updateHarvest
import java.text.SimpleDateFormat
import java.util.*



class FarmEdit : FarmInput() {

    companion object{
        const val DELETED = 6727
        const val DisplayFarmReturnCode = 888762
    }


    lateinit var harvest: Harvest
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)

    var farmID = ""
    var harvestID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        farmID = intent.getStringExtra("FARM_ID") ?: ""
        harvestID = intent.getStringExtra("HARVEST_ID") ?: ""
        title = "Edit Harvest"
        harvestMap[harvestID]?.let {
            harvest = it
        } ?: run {
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            finish()
        }
        loadDataInViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.farmedit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.view_farm_menu){
            val myIntent = Intent(this, FarmDisplay::class.java).apply {
                putExtra("FARM_ID", farmID)
            }
            startActivityForResult(myIntent, DisplayFarmReturnCode )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDataInViews() {
        cropInput.editText?.setText(harvest.crop)
        seedBrandInput.editText?.setText(harvest.seedBrand)
        seedNumInput.editText?.setText(harvest.prevSeedPackets?.toString())
        seedPriceInput.editText?.setText(harvest.prevSeedPrice?.toString())
        sowDateInput.editText?.setText(sdf.format(harvest.sowingDate))
        plantingInput.editText?.setText(harvest.plantingMode)
        weedingInput.editText?.setText(harvest.weedingMode)
        hcmInput.editText?.setText(harvest.harvestCuttingMode)
        workerNumInput.editText?.setText(harvest.prevLabourNum?.toString())
        workerDaysInput.editText?.setText(harvest.prevLabourDays?.toString())
        workerPriceInput.editText?.setText(harvest.prevLabourCharge?.toString())
        machinePriceInput.editText?.setText(harvest.prevMachineryCharge?.toString())
        fertInput.editText?.setText(harvest.fertilizer)
        fertNumInput.editText?.setText(harvest.prevFertilizerPackets?.toString())
        fertPriceInput.editText?.setText(harvest.prevFertilizerPrice?.toString())
        pestInput.editText?.setText(harvest.pesticide)
        pestNumInput.editText?.setText(harvest.prevPesticidePackets?.toString())
        pestPriceInput.editText?.setText(harvest.prevPesticidePrice?.toString())
        commentsInput.editText?.setText(harvest.comment)
    }

    override fun performOperation() {
        val date = sdf.parse(sowDateInput.editText?.text.toString())
        updateHarvest(
            harvestID,
            cropInput.editText?.text.toString(),
            date,
            seedBrandInput.editText?.text.toString(),
            plantingInput.editText?.text.toString(),
            weedingInput.editText?.text.toString(),
            hcmInput.editText?.text.toString(),
            fertInput.editText?.text.toString(),
            pestInput.editText?.text.toString(),
            seedNumInput.editText?.text.toString().toIntOrNull(),
            seedPriceInput.editText?.text.toString().toIntOrNull(),
            workerNumInput.editText?.text.toString().toIntOrNull(),
            workerDaysInput.editText?.text.toString().toIntOrNull(),
            workerPriceInput.editText?.text.toString().toIntOrNull(),
            machinePriceInput.editText?.text.toString().toIntOrNull(),
            fertNumInput.editText?.text.toString().toIntOrNull(),
            fertPriceInput.editText?.text.toString().toIntOrNull(),
            pestNumInput.editText?.text.toString().toIntOrNull(),
            pestPriceInput.editText?.text.toString().toIntOrNull(),
            commentsInput.editText?.text.toString(),
        ) {
            if (it) {
                runOnUiThread { this.finish() }
            }
            else {
                loadingDots.post { loadingDots.visibility = View.GONE }
                saveButton.post { saveButton.text = "Save" }
                runOnUiThread {
                    Toast.makeText(applicationContext, "Unable to save data", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            DisplayFarmReturnCode -> {
                when(resultCode) {
                    DELETED -> finish()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
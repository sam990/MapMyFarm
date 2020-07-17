package com.mapmyfarm.mapmyfarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.mapmyfarm.mapmyfarm.FarmsDataStore.updateFarm
import java.text.SimpleDateFormat
import java.util.*



class FarmEdit : FarmInput() {

    companion object{
        const val DELETED = 6727
    }

    private val DisplayFarmReturnCode = 888762

    lateinit var farm: FarmClass
    val sdf = SimpleDateFormat("yyyy/MM/dd")
    var index = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("hello")
        index = intent.getIntExtra("DATA_INDEX", -1)
        if(index >= 0){
            farm = FarmsDataStore.farmsList[index]
        } else {
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
        if (item.order == 100){
            val myIntent = Intent(this, FarmDisplay::class.java).apply {
                putExtra("DATA_INDEX", index)
            }
            startActivityForResult(myIntent, DisplayFarmReturnCode )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadDataInViews() {
        cropInput.editText?.setText(farm.crop)
        seedBrandInput.editText?.setText(farm.seedBrand)
        seedNumInput.editText?.setText(farm.prevSeedPieces?.toString())
        seedPriceInput.editText?.setText(farm.prevSeedPrice?.toString())
        sowDateInput.editText?.setText(sdf.format(farm.sowingDate))
        plantingInput.editText?.setText(farm.plantingMode)
        weedingInput.editText?.setText(farm.weedingMode)
        hcmInput.editText?.setText(farm.harvestCuttingMode)
        workerNumInput.editText?.setText(farm.prevLabourNum?.toString())
        workerDaysInput.editText?.setText(farm.prevLabourDays?.toString())
        workerPriceInput.editText?.setText(farm.prevLabourCharge?.toString())
        machinePriceInput.editText?.setText(farm.prevMachineryCharge?.toString())
        fertInput.editText?.setText(farm.fertilizer)
        fertNumInput.editText?.setText(farm.prevFertilizerPieces?.toString())
        fertPriceInput.editText?.setText(farm.prevFertilizerPrice?.toString())
        pestInput.editText?.setText(farm.pesticide)
        pestNumInput.editText?.setText(farm.prevPesticidePieces?.toString())
        pestPriceInput.editText?.setText(farm.prevPesticidePrice?.toString())
        landTypeInput.editText?.setText(farm.landType)
        commentsInput.editText?.setText(farm.comment)
    }

    override fun performOperation() {
        val date = sdf.parse(sowDateInput.editText?.text.toString())
        val callback = object: DataStoreOperationCallback{
            override fun onSuccess() {
                finish()
            }

            override fun onError() {
                loadingDots.post { loadingDots.visibility = View.GONE }
                saveButton.post { saveButton.text = "Save" }
                Toast.makeText(applicationContext, "Unable to save data", Toast.LENGTH_LONG).show()
            }

        }
        updateFarm(
            farm.id,
            null,
            date,
            cropInput.editText?.text.toString(),
            seedBrandInput.editText?.text.toString(),
            plantingInput.editText?.text.toString(),
            weedingInput.editText?.text.toString(),
            hcmInput.editText?.text.toString(),
            fertInput.editText?.text.toString(),
            pestInput.editText?.text.toString(),
            landTypeInput.editText?.text.toString(),
            null,
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
            callback
        )
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
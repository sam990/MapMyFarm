package com.mapmyfarm.mapmyfarm

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.eyalbira.loadingdots.LoadingDots
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mapmyfarm.mapmyfarm.FarmsDataStore.addFarm
import com.mapmyfarm.mapmyfarm.FarmsDataStore.addFarmMapping
import com.mapmyfarm.mapmyfarm.FarmsDataStore.addHarvest
import com.mapmyfarm.mapmyfarm.FarmsDataStore.farmMap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class FarmInput : AppCompatActivity() {

    lateinit var saveButton: MaterialButton
    lateinit var cancelButton: MaterialButton

    lateinit var loadingDots: LoadingDots

    lateinit var seedTotal: TextView
    lateinit var labourTotal: TextView
    lateinit var machineTotal: TextView
    lateinit var fertTotal: TextView
    lateinit var pestTotal: TextView

    lateinit var cropInput: TextInputLayout
    lateinit var seedBrandInput: TextInputLayout
    lateinit var seedNumInput: TextInputLayout
    lateinit var seedPriceInput: TextInputLayout
    lateinit var sowDateInput: TextInputLayout
    lateinit var plantingInput: TextInputLayout
    lateinit var weedingInput: TextInputLayout
    lateinit var hcmInput: TextInputLayout
    lateinit var workerNumInput: TextInputLayout
    lateinit var workerDaysInput: TextInputLayout
    lateinit var workerPriceInput: TextInputLayout
    lateinit var machinePriceInput: TextInputLayout
    lateinit var fertInput: TextInputLayout
    lateinit var fertNumInput: TextInputLayout
    lateinit var fertPriceInput: TextInputLayout
    lateinit var pestInput: TextInputLayout
    lateinit var pestNumInput: TextInputLayout
    lateinit var pestPriceInput: TextInputLayout
    lateinit var commentsInput: TextInputLayout


    lateinit var seedSurvey: ViewGroup
    lateinit var labourSurvey: ViewGroup
    lateinit var machineSurvey: ViewGroup
    lateinit var fertSurvey: ViewGroup
    lateinit var pestSurvey: ViewGroup

    lateinit var surveySwitch: Switch


    val totalPrices = IntArray(5)
    var area = 0.0


    lateinit var fID: String
    var farmNumberID = 0

    lateinit var pointsList: ArrayList<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farm_input)
        title = "Harvest Input"
        initialiseButtons()
        initialiseInputs()
        initialiseTextViews()
        initialisePrices()
        initialiseDatePicker()

        fID = intent.getStringExtra("FARM_ID") ?: ""

        if (fID.isEmpty()) {
            Toast.makeText(applicationContext, "Some Error occurred. Please try again later.", Toast.LENGTH_LONG).show()
            finish()
        }

        farmNumberID = farmMap[fID]?.farmID ?: 0
        area = farmMap[fID]?.area ?: 0.0

        // set the farmNumber id
        findViewById<TextView>(R.id.farm_id_text_view).text = farmNumberID.toString().padStart(3, '0')

    }

    private fun initialiseButtons() {
        saveButton = findViewById(R.id.save_button_final)
        cancelButton = findViewById(R.id.cancel_button_final)
        loadingDots = findViewById(R.id.farm_save_loadingdot)
        saveButton.setOnClickListener {
            (it as MaterialButton).text = ""
            it.isEnabled = false
            loadingDots.visibility = View.VISIBLE
            if(!verifyInputs()){
                it.text = getString(R.string.save)
                it.isEnabled = true
                loadingDots.visibility = View.GONE
            } else {
                performOperation()
            }
        }
        cancelButton.setOnClickListener {
            finish()
        }

        surveySwitch = findViewById(R.id.details_switch)
        surveySwitch.setOnCheckedChangeListener { _ , isChecked ->
            if(isChecked) {
                showSurvey()
            } else {
                hideSurvey()
            }
        }
    }

    private fun initialiseInputs() {
        cropInput = findViewById(R.id.crop_input)
        seedBrandInput = findViewById(R.id.seed_brand_input)
        seedNumInput = findViewById(R.id.seednum)
        seedPriceInput = findViewById(R.id.seedprice)

        sowDateInput = findViewById(R.id.sow_date_input)

        plantingInput = findViewById(R.id.planting_mode_input)
        weedingInput = findViewById(R.id.weeding_mode_input)
        hcmInput = findViewById(R.id.hcm_input)

        workerNumInput = findViewById(R.id.workernum)
        workerDaysInput = findViewById(R.id.workerdays)
        workerPriceInput = findViewById(R.id.workerwage)

        machinePriceInput = findViewById(R.id.cost_acre)

        fertInput = findViewById(R.id.fertilizer_input)

        fertNumInput = findViewById(R.id.fertnum)
        fertPriceInput = findViewById(R.id.fertprice)

        pestInput = findViewById(R.id.pesticide_input)

        pestNumInput = findViewById(R.id.pestnum)
        pestPriceInput = findViewById(R.id.pestprice)


        commentsInput = findViewById(R.id.comments_input)

        seedSurvey = findViewById(R.id.seed_survey)
        labourSurvey = findViewById(R.id.labour_survey)
        machineSurvey = findViewById(R.id.machinery_survey)
        fertSurvey = findViewById(R.id.fert_survey)
        pestSurvey = findViewById(R.id.pest_survey)
    }

    private fun initialiseTextViews() {
        seedTotal = findViewById(R.id.seed_total)
        labourTotal = findViewById(R.id.worker_total)
        machineTotal = findViewById(R.id.machine_total)
        fertTotal = findViewById(R.id.fertilizer_total)
        pestTotal = findViewById(R.id.pesticide_total)
    }

    private fun initialisePrices() {
        seedNumInput.editText?.addTextChangedListener {
            updatePrice(it.toString(), seedPriceInput.editText, seedTotal, 0);
        }
        seedPriceInput.editText?.addTextChangedListener {
            updatePrice(it.toString(), seedNumInput.editText, seedTotal, 0)
        }

        fertNumInput.editText?.addTextChangedListener {
            updatePrice(it.toString(), fertPriceInput.editText, fertTotal, 3)
        }
        fertPriceInput.editText?.addTextChangedListener {
            updatePrice(it.toString(), fertNumInput.editText, fertTotal, 3)
        }

        pestNumInput.editText?.addTextChangedListener {
            updatePrice(it.toString(), pestPriceInput.editText, pestTotal, 4)
        }
        pestPriceInput.editText?.addTextChangedListener {
            updatePrice(it.toString(), pestNumInput.editText, pestTotal, 4)
        }

        workerNumInput.editText?.addTextChangedListener {
            updateWorkerPrice(it.toString(), workerDaysInput.editText,
                workerPriceInput.editText, labourTotal, 1)
        }

        workerDaysInput.editText?.addTextChangedListener {
            updateWorkerPrice(it.toString(), workerNumInput.editText,
                workerPriceInput.editText, labourTotal, 1)
        }

        workerPriceInput.editText?.addTextChangedListener {
            updateWorkerPrice(it.toString(), workerDaysInput.editText,
                workerNumInput.editText, labourTotal, 1)
        }

        machinePriceInput.editText?.addTextChangedListener {
            updateMachinePrice(it.toString(), machineTotal , 2)
        }

    }


    //textWatcher method
    private fun updatePrice(current: String, otherInput: EditText?, priceView: TextView, index: Int) {
        if(current.isEmpty() || otherInput?.text?.isEmpty() == true) {
            priceView.text = ""
            totalPrices[index] = 0
            return
        }

        //both have values
        val thisQuan = current.toInt()
        val otherQuan = otherInput?.text.toString().toInt()
        totalPrices[index] = thisQuan * otherQuan
        priceView.text = "Total Rs.${totalPrices[index]}"
    }

    private fun updateWorkerPrice(current: String, other1Input: EditText?,
                          other2Input: EditText?,
                          priceView: TextView, index: Int) {
        if(current.isEmpty() || other1Input?.text?.isEmpty() != false ||
                other2Input?.text?.isEmpty() != false) {
            priceView.text = ""
            totalPrices[index] = 0
            return
        }
        //both have values
        val thisQuan = current.toInt()
        val other1Quan = other1Input?.text.toString().toInt()
        val other2Quan = other2Input?.text.toString().toInt()
        totalPrices[index] = thisQuan * other1Quan * other2Quan
        priceView.text = "Total Rs.${totalPrices[index]}"
    }

    private fun updateMachinePrice(current: String, priceView: TextView, index: Int) {
        if(current.isEmpty() || area == 0.0) {
            priceView.text = ""
            totalPrices[index] = 0
            return
        }
        //both have values
        val thisQuan = current.toInt()
        totalPrices[index] = (thisQuan * area).toInt()
        priceView.text = "Total Rs.${totalPrices[index]}"
    }

    private fun initialiseDatePicker() {

        val listener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val str = "${year}/${month + 1}/${dayOfMonth}"
                sowDateInput.editText?.setText(str)
            }

        val datePicker = DatePickerDialog(this, listener, 2020, 5 , 1 )

        sowDateInput.editText?.setOnClickListener {
            datePicker.show()
        }
    }


    fun verifyInputs() : Boolean {
        var allOkay = true
        if(cropInput.editText?.text?.isEmpty() != false) {
            allOkay = false
            cropInput.error = "This field cannot be empty"
        } else {
            cropInput.error = null
        }
        if (sowDateInput.editText?.text?.isEmpty() != false) {
            allOkay = false
            sowDateInput.error = "This field cannot by empty"
        } else {
            sowDateInput.error = null
        }
        return allOkay
    }

    fun showSurvey() {
        seedSurvey.post {
            seedSurvey.visibility = View.VISIBLE
            labourSurvey.visibility = View.VISIBLE
            machineSurvey.visibility = View.VISIBLE
            fertSurvey.visibility = View.VISIBLE
            pestSurvey.visibility = View.VISIBLE
        }
    }

    fun hideSurvey() {
        seedSurvey.post {
            seedSurvey.visibility = View.GONE
            labourSurvey.visibility = View.GONE
            machineSurvey.visibility = View.GONE
            fertSurvey.visibility = View.GONE
            pestSurvey.visibility = View.GONE
        }
    }

    protected open fun performOperation() {
        val sdf = SimpleDateFormat("yyyy/MM/dd")
        val date = sdf.parse(sowDateInput.editText?.text.toString())

        addHarvest(
            cropInput.editText?.text.toString(),
            date ?: Date(),
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
            if (it == null) {
                // error
                saveError()
            } else {
                addMapping(fID, it)
            }
        }
    }

    fun addMapping(farmID: String, harvestID: String) {
        addFarmMapping(farmID, harvestID) {
            if(it) {
                saveSuccess()
            } else {
                saveError()
            }
        }
    }

    fun saveSuccess() {
        runOnUiThread {
            finish()
        }
    }

    fun saveError() {
        loadingDots.post { loadingDots.visibility = View.GONE }
        saveButton.post { saveButton.text = "Save" }
        runOnUiThread {
            Toast.makeText(applicationContext, "Unable to save data", Toast.LENGTH_LONG).show()
        }
    }
}

package com.mapmyfarm.mapmyfarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails
import com.eyalbira.loadingdots.LoadingDots
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception

open class GetUserDetails : AppCompatActivity() {

    lateinit var firstNameInput : TextInputLayout
    lateinit var lastNameInput : TextInputLayout
    lateinit var localeInput : TextInputLayout
    lateinit var districtInput : TextInputLayout
    lateinit var stateInput : TextInputLayout
    lateinit var emailInput : TextInputLayout
    lateinit var loadingDots: LoadingDots
    lateinit var saveButton: MaterialButton

    var firstNameString: String = ""
    var lastNameString: String = ""
    var localeString: String = ""
    var districtString: String = ""
    var stateString: String = ""
    var emailString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "User Details"
        setContentView(R.layout.activity_get_user_details)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.actionbar_logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        firstNameInput = findViewById(R.id.first_name_input)
        lastNameInput = findViewById(R.id.last_name_input)
        localeInput = findViewById(R.id.locale_input)
        districtInput = findViewById(R.id.district_input)
        stateInput = findViewById(R.id.state_input)
        emailInput = findViewById(R.id.email_input)
        loadingDots = findViewById(R.id.details_save_loadingdots)
        saveButton = findViewById(R.id.user_details_save)
        saveButton.setOnClickListener {
            (it as MaterialButton).text = ""
            loadingDots.visibility = View.VISIBLE
            if(!verifyInputs()){
                it.text = getString(R.string.save)
                loadingDots.visibility = View.GONE
            } else {
                saveDetails()
            }
        }
    }

    private fun verifyInputs() : Boolean {
        var allOkay = true
        firstNameString = firstNameInput.editText?.text.toString()
        if (firstNameString.isEmpty()){
            firstNameInput.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            firstNameInput.error = null
        }

        lastNameString = lastNameInput.editText?.text.toString()
        if (lastNameString.isEmpty()){
            lastNameInput.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            lastNameInput.error = null
        }

        localeString = localeInput.editText?.text.toString()
        if(localeString.isEmpty()) {
            localeInput.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            localeInput.error = null
        }

        districtString = districtInput.editText?.text.toString()
        if(districtString.isEmpty()) {
            districtInput.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            districtInput.error = null
        }

        stateString = stateInput.editText?.text.toString()
        if(stateString.isEmpty()){
            stateInput.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            stateInput.error = null
        }

        emailString = emailInput.editText?.text.toString()
        if(emailString.isNotEmpty() &&
                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()){
            emailInput.error = getString(R.string.email_not_valid)
            allOkay = false
        } else {
            emailInput.error = null
        }

        return allOkay
    }

    private fun saveDetails() {
        val client = AWSMobileClient.getInstance()
        val map = HashMap<String, String>()
        map[FIRST_NAME] = firstNameString
        map[LAST_NAME] = lastNameString
        map[LOCALE] = localeString
        map[DISTRICT] = districtString
        map[STATE] = stateString
        map[DETAILS] = "1"
        if (emailString.isNotEmpty()) {
            map[EMAIL] = emailString
        }
        client.updateUserAttributes(map, object : Callback<List<UserCodeDeliveryDetails>> {
            override fun onResult(result: List<UserCodeDeliveryDetails>?) {
                // successfull
                runOnUiThread { successUpdate() }
            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
                runOnUiThread {
                    errorUpdate()
                }
            }
        })
    }

    //
    protected open fun successUpdate() {
        loadingDots.visibility = View.GONE
        saveButton.text = getString(R.string.save)
        saveUserDetailsLocal(this, firstNameString , lastNameString,
        localeString , districtString , stateString , emailString)
        //go to the next page
        val myIntent = Intent(this, Dashboard::class.java)
        myIntent.putExtra("LOAD_FROM_SERVER", true)
        startActivity(myIntent)
        finish()
    }

    private fun errorUpdate() {
        Toast.makeText(applicationContext, getString(R.string.err_saving_details), Toast.LENGTH_LONG).show()
        loadingDots.visibility = View.GONE
        saveButton.text = getString(R.string.save)
    }
}

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

class GetUserDetails : AppCompatActivity() {

    lateinit var firstName : TextInputLayout
    lateinit var lastName : TextInputLayout
    lateinit var locale : TextInputLayout
    lateinit var district : TextInputLayout
    lateinit var state : TextInputLayout
    lateinit var email : TextInputLayout
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

        firstName = findViewById(R.id.first_name_input)
        lastName = findViewById(R.id.last_name_input)
        locale = findViewById(R.id.locale_input)
        district = findViewById(R.id.district_input)
        state = findViewById(R.id.state_input)
        email = findViewById(R.id.email_input)
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
        firstNameString = firstName.editText?.text.toString()
        if (firstNameString.isEmpty()){
            firstName.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            firstName.error = null
        }

        lastNameString = lastName.editText?.text.toString()
        if (lastNameString.isEmpty()){
            lastName.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            lastName.error = null
        }

        localeString = locale.editText?.text.toString()
        if(localeString.isEmpty()) {
            locale.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            locale.error = null
        }

        districtString = district.editText?.text.toString()
        if(districtString.isEmpty()) {
            district.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            district.error = null
        }

        stateString = state.editText?.text.toString()
        if(stateString.isEmpty()){
            state.error = getString(R.string.empty_field)
            allOkay = false
        } else {
            state.error = null
        }

        emailString = email.editText?.text.toString()
        if(emailString.isNotEmpty() &&
                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()){
            email.error = getString(R.string.email_not_valid)
            allOkay = false
        } else {
            email.error = null
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
    private fun successUpdate() {
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

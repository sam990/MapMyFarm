package com.mapmyfarm.mapmyfarm

import android.os.Bundle
import android.view.View
import android.widget.Toast

class UpdateUserDetails: GetUserDetails() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDetails()
    }

    private fun loadDetails() {
        firstNameInput.editText?.setText(firstName)
        lastNameInput.editText?.setText(lastName)
        localeInput.editText?.setText(locale)
        districtInput.editText?.setText(district)
        stateInput.editText?.setText(state)
        emailInput.editText?.setText(email)
    }

    override fun successUpdate() {
        Toast.makeText(applicationContext, "Details saved successfully", Toast.LENGTH_LONG).show()
        loadingDots.visibility = View.GONE
        saveButton.text = getString(R.string.save)
        saveUserDetailsLocal(this, firstNameString , lastNameString,
            localeString , districtString , stateString , emailString)
    }
}
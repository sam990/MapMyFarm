package com.mapmyfarm.mapmyfarm

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import java.lang.Exception

const val PREFERENCE_NAME = "com.mapmyfarm.mapmyfarm.userpref"

//constants
const val FIRST_NAME = "name"
const val LAST_NAME = "family_name"
const val LOCALE = "locale"
const val DISTRICT = "custom:district"
const val STATE = "custom:state"
const val EMAIL = "email"
const val DETAILS = "custom:detailsEntered"

@Volatile var firstName : String = ""
@Volatile var lastName : String = ""
@Volatile var locale : String = ""
@Volatile var district : String = ""
@Volatile var state : String = ""
@Volatile var email : String = ""

interface InitialiseUserDetailsCallback {
    fun onResult(result: Boolean)
}

fun initialiseUserDetails(context: Context, callback: InitialiseUserDetailsCallback){
    AWSMobileClient.getInstance().getUserAttributes(object : Callback<MutableMap<String, String>>{
        override fun onResult(result: MutableMap<String, String>?) {
            if(result == null) {
                callback.onResult(false)
                return
            }

            if (result[DETAILS] == "0"){
                callback.onResult(false)
                return
            }

            firstName = result[FIRST_NAME] ?: ""
            lastName = result[LAST_NAME] ?: ""
            locale = result[LOCALE] ?: ""
            district = result[DISTRICT] ?: ""
            state = result[STATE] ?: ""
            email = result[EMAIL] ?: ""
            //else fill all the attributes
            val sharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            sharedPref.edit {
                this.putString(FIRST_NAME, firstName)
                this.putString(LAST_NAME, lastName)
                this.putString(LOCALE, locale)
                this.putString(DISTRICT, district)
                this.putString(STATE, state)
                this.putString(EMAIL, email)
                this.apply()
            }
            callback.onResult(true)
        }

        override fun onError(e: Exception?) {
            callback.onResult(false)
        }

    })

}

fun saveUserDetailsLocal(context: Context, fName: String, lName: String, loc: String,
                        dis : String, st : String, em : String){
    val sharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    sharedPref.edit {
        this.putString(FIRST_NAME, fName)
        this.putString(LAST_NAME, lName)
        this.putString(LOCALE, loc)
        this.putString(DISTRICT, dis)
        this.putString(STATE, st)
        this.putString(EMAIL, em)
        this.apply()
    }
}

fun loadUserDetailsLocal(context: Context, callback: InitialiseUserDetailsCallback) {
    Thread {
        val sharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

        with(sharedPref){
            firstName = getString(FIRST_NAME, "") ?: ""
            lastName = getString(LAST_NAME, "") ?: ""
            locale = getString(LOCALE, "") ?: ""
            district = getString(DISTRICT, "") ?: ""
            state = getString(STATE, "") ?: ""
            email = getString(EMAIL, "") ?: ""
        }

        //check for empty
        if(firstName == ""|| lastName == "" || locale == ""
            || district == "" || state == ""){
            callback.onResult(false)
        }
        else {
            callback.onResult(true)
        }
    }.start()
}


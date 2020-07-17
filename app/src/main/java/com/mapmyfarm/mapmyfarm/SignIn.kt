package com.mapmyfarm.mapmyfarm

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobile.client.results.SignUpResult
import com.amazonaws.services.cognitoidentityprovider.model.UserLambdaValidationException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.eyalbira.loadingdots.LoadingDots
import com.google.android.material.button.MaterialButton
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class SignIn : AppCompatActivity() {

    lateinit var phoneInput : MyPhoneInputLayout
    lateinit var phoneConfirm : MaterialButton
    lateinit var loadingDots: LoadingDots

    val OTPRequestCode = 990
    companion object{
        const val PHONE = "PHONE"
    }


    val notExistError = "DefineAuthChallenge failed with error User does not exist."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        phoneInput = findViewById(R.id.phone_input)
        phoneInput.setDefaultCountry("IN")
        phoneConfirm = findViewById(R.id.phone_confirm)
        loadingDots = findViewById(R.id.phone_confirm_loadingdots)
        with(phoneInput) {
            addNumberChangeListener(object : MyPhoneInputLayout.onNumberChangeListener{
                override fun onInvalid() {
                    phoneConfirm.isEnabled = false
                }

                override fun onValid() {
                    phoneConfirm.isEnabled = true
                    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(phoneInput.windowToken, 0)
                }

            })
        }
        phoneConfirm.setOnClickListener {
            setButtonToLoading()
            makeSignInRequest()
        }
    }

    private fun setButtonToLoading(){
        phoneConfirm.text = ""
        phoneConfirm.isEnabled = false
        loadingDots.visibility = View.VISIBLE
    }

    private fun setButtonToNormal(){
        loadingDots.visibility = View.GONE
        phoneConfirm.isEnabled = true
        phoneConfirm.text = getString(R.string.confirm)
    }

    fun makeSignInRequest() {

        val awsClient = AWSMobileClient.getInstance()

        awsClient.signIn(phoneInput.phoneNumber, "abcdefghi", null, object : Callback<SignInResult>{
            override fun onResult(result: SignInResult?) {
                println(result?.signInState.toString())
                when(result?.signInState){
                    SignInState.CUSTOM_CHALLENGE -> {
                        verifyOTP()
                    }
                    else -> {
                        handleError()
                    }
                }
            }

            override fun onError(e: Exception?) {
                when(e){
                    is UserNotFoundException -> signUp()
                    is UserLambdaValidationException -> {
                        if (e.errorMessage == notExistError){
                            signUp()
                        }
                        else {
                            //some other error
                            handleError()
                        }
                    }
                    else -> {
                        e?.printStackTrace()
                        handleError()
                    }
                }
            }

        })

    }

    private fun signUp(){
        val attr = HashMap<String, String>()
        attr["custom:detailsEntered"] = "0"
        val awsClient = AWSMobileClient.getInstance()
        awsClient.signUp(phoneInput.phoneNumber, UUID.randomUUID().toString(), attr, null, object : Callback<SignUpResult>{
            override fun onResult(result: SignUpResult?) {
                println(result.toString())
                makeSignInRequest()
            }

            override fun onError(e: Exception?) {

                e?.printStackTrace()
                handleError()
            }
        })
    }

    fun handleError(){
        runOnUiThread{
            setButtonToNormal()
            Toast.makeText(applicationContext, "Unable to process your request. Please try again later", Toast.LENGTH_LONG).show()
        }
    }

    fun verifyOTP(){
        //set button to normal
        runOnUiThread{
            setButtonToNormal()
        }
        val myIntent = Intent(this, GetOTP::class.java)
        myIntent.putExtra(PHONE, phoneInput.phoneNumber)
        startActivityForResult(myIntent, OTPRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == OTPRequestCode){
            if (resultCode == Activity.RESULT_OK){
                val myIntent = Intent(this, Dashboard::class.java)
                startActivity(myIntent)
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}

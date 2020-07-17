package com.mapmyfarm.mapmyfarm

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoServiceConstants
import com.amazonaws.services.cognitoidentityprovider.model.UserLambdaValidationException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.amazonaws.services.cognitoidentityprovider.model.transform.UserLambdaValidationExceptionUnmarshaller
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.mukesh.OtpView
import java.util.*
import kotlin.collections.HashMap


class GetOTP : AppCompatActivity() {

    lateinit var numberView : TextView
    lateinit var otpView: OtpView
    lateinit var attemptsView : TextView
    lateinit var resendButton : TextView
    lateinit var secondsView : TextView
    lateinit var progressView : ProgressBar
    lateinit var progressOk : ImageView
    lateinit var otpStatus : TextView
    lateinit var receiver: SMSBroadcastReceiver

    private val intervals = longArrayOf(15000 , 30000 , 60000)
    private var numAttempts = 3
    private var resendCounter = 0
    private lateinit var phoneNum : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_otp)
        numberView = findViewById(R.id.mob_number)
        phoneNum = (intent.getStringExtra(SignIn.PHONE)) ?: ""
        numberView.text = phoneNum
        otpView = findViewById(R.id.otp_enter_view)
        otpView.setOtpCompletionListener {
            //hide keyboard
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(otpView.windowToken, 0)
            verifyOTP(otpView.text.toString())
        }
        receiver = SMSBroadcastReceiver()
        receiver.smsListener = object : SMSBroadcastReceiver.SMSListener{
            override fun onReceive(otp: String) {
                otpView.setText(otp)
            }

            override fun onTimeout() {

            }

        }
        val client = SmsRetriever.getClient(this)
//        Start the SMS Retriever task
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
//            if successfully started, then start the receiver.
            registerReceiver(
                receiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            )

        }

        task.addOnFailureListener { e ->
            //            if failure print the exception.
            e.printStackTrace()
        }
        resendButton = findViewById(R.id.resend_otp_button)
        secondsView = findViewById(R.id.resend_seconds)
        progressView = findViewById(R.id.otp_confirm_progress)
        progressOk = findViewById(R.id.otp_right_tick)
        otpStatus = findViewById(R.id.otp_status_message)
        attemptsView = findViewById(R.id.remaining_attempts_num)

        findViewById<TextView>(R.id.change_number).setOnClickListener {
            unsuccessfulOTP()
        }

        resendButton.setOnClickListener {
            resendOTP()
        }

        setResendTimer()
    }

    private fun setResendTimer() {
        resendButton.isEnabled = false
        //get the time
        val time = if(resendCounter < 3) intervals[resendCounter] else intervals[2]
        secondsView.visibility = View.VISIBLE

        object: CountDownTimer(time , 1000) {
            override fun onFinish() {
                secondsView.visibility = View.INVISIBLE
                resendButton.isEnabled = true
                resendCounter++
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsView.text = "(${millisUntilFinished / 1000})"
            }

        }.start()

    }

    private fun resendOTPSuccessful() {
        otpStatus.text = getString(R.string.otp_sent)
        progressView.visibility = View.GONE
        otpStatus.visibility = View.VISIBLE
        otpStatus.postDelayed({
            otpStatus.visibility = View.GONE
        }, 3000)
        numAttempts = 3
        attemptsView.text = numAttempts.toString()
        setResendTimer()
    }

    private fun resendOTPError() {
        otpStatus.text = getString(R.string.otp_sent_error)
        progressView.visibility = View.GONE
        otpStatus.visibility = View.VISIBLE
        otpStatus.postDelayed({
            otpStatus.visibility = View.GONE
        }, 3000)
    }

    private fun resendOTP() {
        otpStatus.visibility = View.GONE
        progressView.visibility = View.VISIBLE

        val awsClient = AWSMobileClient.getInstance()

        awsClient.signIn(phoneNum, UUID.randomUUID().toString().substring(0, 15) , null, object : Callback<SignInResult>{
            override fun onResult(result: SignInResult?) {
                println(result?.signInState.toString())
                when(result?.signInState){
                    SignInState.CUSTOM_CHALLENGE -> {
                        runOnUiThread {
                            resendOTPSuccessful()
                        }
                    }
                    else -> {
                        runOnUiThread {
                            resendOTPError()
                        }
                    }
                }
            }

            override fun onError(e: java.lang.Exception?) {
                e?.printStackTrace()
                runOnUiThread {
                    resendOTPError()
                }
            }

        })
    }


    private fun verifyOTPError(){
        runOnUiThread {
            otpStatus.text = getString(R.string.wrong_otp)
            progressView.visibility = View.GONE
            otpStatus.visibility = View.VISIBLE
            otpStatus.postDelayed({
                otpStatus.visibility = View.GONE
            }, 3000)
            numAttempts--
            if (numAttempts == 0){
                unsuccessfulOTP()
            }
            attemptsView.text = numAttempts.toString()
            otpView.setText("")
        }

    }

    private fun verifyOTPSuccessful() {
        runOnUiThread {
            progressView.visibility = View.GONE
            progressOk.visibility = View.VISIBLE
        }
        initialiseUserDetails(this, object : InitialiseUserDetailsCallback{
            override fun onResult(result: Boolean) {
                if(result) {
                    runOnUiThread {successfulOTP()}
                }
                else {
                    runOnUiThread {
                        getUserDetails()
                    }

                }
            }

        })
    }

    private fun verifyOTP(num : String) {
        //set progress
        otpStatus.visibility = View.GONE
        progressView.visibility = View.VISIBLE

        val awsMobileClient = AWSMobileClient.getInstance()
        val res = HashMap<String, String>()
        res[CognitoServiceConstants.CHLG_RESP_ANSWER] = num
        val meta = HashMap<String, String>()
        awsMobileClient.confirmSignIn(res, meta, object : Callback<SignInResult>{
            override fun onResult(result: SignInResult?) {
                when (result?.signInState){
                    SignInState.CUSTOM_CHALLENGE -> {
                        verifyOTPError()
                    }
                    SignInState.DONE -> {
                        verifyOTPSuccessful()
                    }
                    else -> {
                        verifyOTPError()
                    }
                }
            }

            override fun onError(e: Exception?) {
                when(e){
                    is UserLambdaValidationException -> {
                        unsuccessfulOTP()
                    }
                    else -> verifyOTPError()
                }
            }

        })
    }

    private fun unsuccessfulOTP() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun successfulOTP() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun getUserDetails() {
        val myIntent = Intent(this, GetUserDetails::class.java)
        startActivity(myIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

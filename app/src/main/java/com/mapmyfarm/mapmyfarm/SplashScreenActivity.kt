package com.mapmyfarm.mapmyfarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import java.lang.Exception

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appSignatureHelper = AppSignatureHelper(applicationContext)
        appSignatureHelper.appSignatures;
        val callback = object : Callback<UserStateDetails>{
            override fun onResult(result: UserStateDetails?) {
                when (result?.userState) {
                    UserState.SIGNED_IN -> {
                        println("Signed in")
                        AWSMobileClient.getInstance().signOut()
                        recreate() //for now
                    }
                    UserState.SIGNED_OUT-> {
                        val intent = Intent(this@SplashScreenActivity, SignIn::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        AWSMobileClient.getInstance().signOut()
                        recreate()
                    }
                }

            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
            }

        }
        AWSMobileClient.getInstance().initialize(this, callback)
    }
}

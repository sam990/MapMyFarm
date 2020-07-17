package com.mapmyfarm.mapmyfarm

import android.content.Context
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider


object AppSyncClientFactory {
    @Volatile
    private var client: AWSAppSyncClient? = null

    @Synchronized
    fun init(context: Context?) {
        val awsConfiguration = AWSConfiguration(context)
        client = AWSAppSyncClient.builder()
            .context(context)
            .awsConfiguration(awsConfiguration)
            .cognitoUserPoolsAuthProvider(CognitoUserPoolsAuthProvider {
                try {
                    return@CognitoUserPoolsAuthProvider AWSMobileClient.getInstance().tokens.idToken
                        .tokenString
                } catch (e: Exception) {
                    Log.e("APPSYNC_ERROR", e.localizedMessage)
                    return@CognitoUserPoolsAuthProvider e.localizedMessage
                }
            }).build()
    }

    @Synchronized
    fun appSyncClient(): AWSAppSyncClient? {
        return client
    }
}
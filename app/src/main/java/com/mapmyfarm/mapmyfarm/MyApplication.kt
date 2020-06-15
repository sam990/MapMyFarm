package com.mapmyfarm.mapmyfarm

import android.app.Application
import android.content.Context
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender


@AcraCore(buildConfigClass = BuildConfig::class)
@AcraMailSender(mailTo = "sameerahmad990@gmail.com", reportAsFile = true)
class MyApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }
}
package com.nejracoric.securepassandroid

import android.app.Application
import com.nejracoric.securepassandroid.security.PwnedPasswordChecker

class SecurePassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PwnedPasswordChecker.init()
    }
}

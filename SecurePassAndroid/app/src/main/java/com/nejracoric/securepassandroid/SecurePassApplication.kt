package com.nejracoric.securepassandroid

import android.app.Application
import com.nejracoric.securepassandroid.data.local.TokenManager
import com.nejracoric.securepassandroid.security.PwnedPasswordChecker
import kotlinx.coroutines.runBlocking

class SecurePassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PwnedPasswordChecker.init()
        runBlocking {
            TokenManager(this@SecurePassApplication).getToken()
        }
    }
}

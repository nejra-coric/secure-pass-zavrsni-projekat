package com.nejracoric.securepassandroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.nejracoric.securepassandroid.ui.navigation.AppNavigation
import com.nejracoric.securepassandroid.ui.theme.SecurePassAndroidTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecurePassAndroidTheme {
                AppNavigation()
            }
        }
    }
}

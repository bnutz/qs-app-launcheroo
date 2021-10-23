package com.justbnutz.quicksettingsapplauncheroo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity for showing a splash screen while we're waiting for the app to load.
 * Reference: https://android.jlelse.eu/right-way-to-create-splash-screen-on-android-e7f1709ba154
 */
class SplashScreenActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
package com.justbnutz.quicksettingsapplauncheroo.utils

import android.content.SharedPreferences
import androidx.annotation.IntRange
import androidx.core.content.edit

object PrefHelper {
    private const val THRESHOLD_SUFFIX = "_THRESHOLD"
    private const val INVERT_SUFFIX = "_INVERT"

    private const val DEFAULT_THRESHOLD = 225

    private lateinit var sharedPrefs: SharedPreferences

    fun init(defaultPreferences: SharedPreferences) {
        sharedPrefs = defaultPreferences
    }

    fun checkInit(defaultPreferences: SharedPreferences) {
        if (!this::sharedPrefs.isInitialized) sharedPrefs = defaultPreferences
    }

    fun getTilePackageName(serviceTag: String): String? = sharedPrefs.getString(serviceTag, null)

    fun setTilePackageName(serviceTag: String, packageName: String) = sharedPrefs.edit {
        putString(serviceTag, packageName)
    }

    fun removeTilePackageName(serviceTag: String) = sharedPrefs.edit {
        remove(serviceTag)
    }

    fun getIconThreshold(packageName: String): Int = sharedPrefs.getInt("$packageName$THRESHOLD_SUFFIX", DEFAULT_THRESHOLD)

    fun setIconThreshold(packageName: String, @IntRange(from = 0, to = 255) threshold: Int) = sharedPrefs.edit {
        putInt("$packageName$THRESHOLD_SUFFIX", threshold)
    }

    fun getIconInvert(packageName: String): Boolean = sharedPrefs.getBoolean("$packageName$INVERT_SUFFIX", false)

    fun setIconInvert(packageName: String, invert: Boolean) = sharedPrefs.edit {
        putBoolean("$packageName$INVERT_SUFFIX", invert)
    }

    fun isTunedIcon(packageName: String): Boolean = sharedPrefs.contains("$packageName$THRESHOLD_SUFFIX") || sharedPrefs.contains("$packageName$INVERT_SUFFIX")
}
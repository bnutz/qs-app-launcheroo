package com.justbnutz.quicksettingsapplauncheroo.models

import android.graphics.drawable.Drawable

data class AppItemModel(
    val appName: String,
    val appIcon: Drawable?,
    val packageName: String,
    val isSystemApp: Boolean
)
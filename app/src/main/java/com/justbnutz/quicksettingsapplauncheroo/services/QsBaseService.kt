package com.justbnutz.quicksettingsapplauncheroo.services

import android.app.AlertDialog
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.preference.PreferenceManager
import com.justbnutz.quicksettingsapplauncheroo.R
import com.justbnutz.quicksettingsapplauncheroo.utils.PrefHelper
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils

abstract class QsBaseService : TileService() {

    private fun checkPrefHelper() {
        PrefHelper.checkInit(PreferenceManager.getDefaultSharedPreferences(this))
    }

    private fun updateTileProperties() = qsTile?.let {
        checkPrefHelper()
        PrefHelper.getTilePackageName(getTag())?.let { pkgName ->
            packageManager?.let { pm -> Utils.buildAppItemModel(pm, pkgName)?.let { appInfo ->
                it.label = appInfo.appName
                it.icon = appInfo.appIcon?.let { icon ->
                    val monoIcon = Utils.convertMonoIcon(icon,
                        PrefHelper.getIconThreshold(pkgName),
                        PrefHelper.getIconInvert(pkgName)
                    )
                    Icon.createWithBitmap(monoIcon)
                }
                it.updateTile()
            } }
        }
    }

    // No need to keep state
    private fun keepDisabled() = qsTile?.let {
        it.state = Tile.STATE_INACTIVE
        it.updateTile()
    }

    abstract fun getTag(): String

    override fun onCreate() {
        super.onCreate()
        updateTileProperties()
        keepDisabled()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileProperties()
        keepDisabled()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileProperties()
        keepDisabled()
    }

    override fun onClick() {
        super.onClick()
        try {
            checkPrefHelper()
            PrefHelper.getTilePackageName(getTag())?.let { packageName ->
                packageManager?.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
                    startActivityAndCollapse(launchIntent)
                    keepDisabled()
                }
            } ?: kotlin.run {
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.no_package)
                    .create()
                showDialog(dialog)
            }
        } catch (e: Exception) {
            Log.d("QS_CHECK", getString(R.string.error_message, e.message))
        }
    }
}
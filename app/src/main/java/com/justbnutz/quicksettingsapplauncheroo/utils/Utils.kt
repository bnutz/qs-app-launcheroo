package com.justbnutz.quicksettingsapplauncheroo.utils

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.annotation.IntRange
import androidx.core.graphics.drawable.toBitmap
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Result

object Utils {
    fun buildAppDetailsIntent(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            it.addCategory(Intent.CATEGORY_DEFAULT)
            it.data = Uri.parse("package:$packageName")
        }

    fun buildAppItemModel(packageManager: PackageManager, packageName: String): AppItemModel? {
        val result = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appItem = AppItemModel(
                appName,
                packageManager.getApplicationIcon(appInfo),
                packageName,
                (appInfo.flags.and(ApplicationInfo.FLAG_SYSTEM)) == ApplicationInfo.FLAG_SYSTEM,
            )

            Result.Success(appItem)
        } catch (e: Exception) {
            Result.Error(e)
        }

        return when (result) {
            is Result.Success -> result.data
            is Result.Error -> null
        }
    }

    fun convertMonoIcon(appIcon: Drawable, @IntRange(from = 0, to = 255) threshold: Int, invert: Boolean = false): Bitmap {
        val bmpWidth = appIcon.intrinsicWidth
        val bmpHeight = appIcon.intrinsicHeight
        val monoBmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(monoBmp)

        // Convert icon to greyscale (https://stackoverflow.com/a/9377943)
        val colourMatrix = ColorMatrix()
        colourMatrix.setSaturation(0f)

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colourMatrix)
        canvas.drawBitmap(appIcon.toBitmap(), 0f, 0f, paint)

        // Put the pixel colours into an array and set anything that crosses the threshold value to transparent (https://stackoverflow.com/a/9523921)
        val pixArray = IntArray(bmpWidth * bmpHeight)
        monoBmp.getPixels(pixArray, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight)
        for (x in 0 until bmpWidth) {
            for (y in 0 until bmpHeight) {
                val index = y * bmpWidth + x
                with(pixArray[index]) {
                    if (this != Color.TRANSPARENT) {
                        pixArray[index] =
                            if (crossThreshold(this, threshold, invert)) Color.TRANSPARENT
                            else Color.BLACK
                    }
                }
            }
        }
        return Bitmap.createBitmap(pixArray, bmpWidth, bmpHeight, Bitmap.Config.ALPHA_8)
    }

    private fun crossThreshold(@IntRange(from = 0, to = 255) colour: Int, @IntRange(from = 0, to = 255) threshold: Int, invert: Boolean): Boolean {
        return if (!invert) (Color.red(colour) >= threshold && Color.green(colour) >= threshold && Color.blue(colour) >= threshold)
        else (Color.red(colour) <= threshold && Color.green(colour) <= threshold && Color.blue(colour) <= threshold)
    }
}
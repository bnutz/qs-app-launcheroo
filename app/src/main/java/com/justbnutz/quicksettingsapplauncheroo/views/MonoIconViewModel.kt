package com.justbnutz.quicksettingsapplauncheroo.views

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils
import kotlinx.coroutines.launch

class MonoIconViewModel : ViewModel() {
    var appData: AppItemModel? = null

    // === Continuous Monitoring ===
    private val _monoAppIcon by lazy { MutableLiveData<Bitmap>() }
    val monoAppIcon: LiveData<Bitmap>
        get() = _monoAppIcon

    // === Single Events ===
    private val _monoThreshold by lazy { MutableLiveData<Event<Int>>() }
    val monoThreshold: LiveData<Event<Int>>
        get() = _monoThreshold
    private val _monoInvert by lazy { MutableLiveData<Event<Boolean>>() }
    val monoInvert: LiveData<Event<Boolean>>
        get() = _monoInvert

    fun runMonoConversion(threshold: Int, invert: Boolean) {
        viewModelScope.launch {
            appData?.appIcon?.let { appIcon ->
                val icon = Utils.convertMonoIcon(appIcon, threshold, invert)
                _monoAppIcon.postValue(icon)
            }
        }
    }
}
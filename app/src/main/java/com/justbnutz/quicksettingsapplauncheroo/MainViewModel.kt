package com.justbnutz.quicksettingsapplauncheroo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event

/**
 * Share data between fragments using ViewModels:
 * https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
 */
class MainViewModel : ViewModel() {

    private val _assignPosition by lazy { MutableLiveData<Event<String>>() }
    val assignPosition: LiveData<Event<String>>
        get() = _assignPosition

    private val _selectedItem by lazy { MutableLiveData<Event<Pair<String, AppItemModel>>>() }
    val selectedItem: LiveData<Event<Pair<String, AppItemModel>>>
        get() = _selectedItem

    fun assignPosition(serviceTag: String) {
        _assignPosition.postValue(Event(serviceTag))
    }

    fun selectedItem(appData: AppItemModel) {
        assignPosition.value?.peekData()?.let { tag ->
            _selectedItem.postValue(Event(Pair(tag, appData)))
        }
    }
}
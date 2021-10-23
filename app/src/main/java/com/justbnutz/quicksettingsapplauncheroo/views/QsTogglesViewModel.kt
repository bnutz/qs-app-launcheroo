package com.justbnutz.quicksettingsapplauncheroo.views

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.models.ShortcutItemModel
import com.justbnutz.quicksettingsapplauncheroo.utils.PrefHelper
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QsTogglesViewModel : ViewModel() {
    lateinit var packageManager: PackageManager

    // === Continuous Monitoring ===
    private val _shortcutList by lazy { MutableLiveData<List<ShortcutItemModel>>() }
    val shortcutList: LiveData<List<ShortcutItemModel>>
        get() = _shortcutList

    private val _updateTile by lazy { MutableLiveData<Pair<String, Boolean>>() }
    val updateTile: LiveData<Pair<String, Boolean>>
        get() = _updateTile

    // === Single Events ===
    private val _removedItem by lazy { MutableLiveData<Event<ShortcutItemModel>>() }
    val removedItem: LiveData<Event<ShortcutItemModel>>
        get() = _removedItem

    private val _addedItem by lazy { MutableLiveData<Event<ShortcutItemModel>>() }
    val addedItem: LiveData<Event<ShortcutItemModel>>
        get() = _addedItem

    fun initToggleList(serviceTags: List<String>) {
        viewModelScope.launch {
            val newList = mutableListOf<ShortcutItemModel>()
            serviceTags.forEach { tag ->
                val appData = PrefHelper.getTilePackageName(tag)?.let { packageName -> getAppInfo(packageName) }
                newList.add(ShortcutItemModel(tag, appData))
                _updateTile.postValue(Pair(tag, appData != null))
            }
            _shortcutList.postValue(newList)
        }
    }

    fun updateToggleItem(serviceTag: String, appData: AppItemModel? = null, notifyAdded: Boolean = false) {
        viewModelScope.launch {
            shortcutList.value?.toMutableList()?.let { newList ->
                // (Needs to be new object for DiffUtil to be able to compare with old object)
                newList.indexOfFirst { it.serviceTag == serviceTag }.takeIf { it >= 0 }?.let { index ->
                    // If we're removing the item, make a copy to post back for the undo action
                    if (appData == null) newList[index].takeIf { it.appItem != null }?.let { oldItem ->
                        _removedItem.postValue(Event(oldItem))
                    } else if (notifyAdded) {
                        _addedItem.postValue(Event(ShortcutItemModel(serviceTag, appData)))
                    }

                    newList.removeAt(index)
                    newList.add(index, ShortcutItemModel(serviceTag, appData))

                    if (appData != null) PrefHelper.setTilePackageName(serviceTag, appData.packageName)
                    else PrefHelper.removeTilePackageName(serviceTag)
                }
                _shortcutList.postValue(newList)
                _updateTile.postValue(Pair(serviceTag, appData != null))
            }
        }
    }

    private suspend fun getAppInfo(packageName: String): AppItemModel? = withContext(Dispatchers.Default) {
        Utils.buildAppItemModel(packageManager, packageName)
    }
}
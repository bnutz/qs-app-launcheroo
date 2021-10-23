package com.justbnutz.quicksettingsapplauncheroo.views

import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.models.Result
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils
import kotlinx.coroutines.launch

class AppListViewModel : ViewModel() {
    lateinit var packageManager: PackageManager

    private val _appList by lazy { MutableLiveData<List<AppItemModel>>() }
    val appList: LiveData<List<AppItemModel>>
        get() = _appList

    private val _errorMsg by lazy { MutableLiveData<Event<String>>() }
    val errorMsg: LiveData<Event<String>>
        get() = _errorMsg

    fun getAppList(searchTerm: String = "") {
        viewModelScope.launch {
            // https://developer.android.com/kotlin/coroutines#handling-exceptions
            val result = try {
                val appList = mutableListOf<AppItemModel>()

                val launcherIntent = Intent(Intent.ACTION_MAIN).also {
                    it.addCategory(Intent.CATEGORY_LAUNCHER)
                }

                packageManager.let { pm ->
                    pm.queryIntentActivities(launcherIntent, 0).filterNotNull().forEach {resolveInfo ->
                        resolveInfo.activityInfo?.packageName?.let { packageName ->
                            Utils.buildAppItemModel(pm, packageName)?.let { appData ->
                                if (searchTerm.isEmpty()
                                    ||appData.appName.contains(searchTerm, true)
                                    ||packageName.contains(searchTerm, true)) {
                                    appList.add(appData)
                                }
                            }
                        }
                    }
                }
                appList.sortedBy { it.appName }

                Result.Success(appList)
            } catch (e: Exception) {
                Result.Error(e)
            }

            when (result) {
                is Result.Success -> _appList.postValue(result.data)
                is Result.Error -> _errorMsg.postValue(Event(result.exception.message ?: ""))
            }
        }
    }
}
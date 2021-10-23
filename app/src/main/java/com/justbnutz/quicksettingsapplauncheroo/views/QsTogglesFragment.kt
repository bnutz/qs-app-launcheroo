package com.justbnutz.quicksettingsapplauncheroo.views

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.justbnutz.quicksettingsapplauncheroo.MainActivity
import com.justbnutz.quicksettingsapplauncheroo.MainViewModel
import com.justbnutz.quicksettingsapplauncheroo.R
import com.justbnutz.quicksettingsapplauncheroo.databinding.FragmentQsTogglesBinding
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.models.ShortcutItemModel
import com.justbnutz.quicksettingsapplauncheroo.services.*
import com.justbnutz.quicksettingsapplauncheroo.utils.PrefHelper
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils

class QsTogglesFragment : BaseFragment<FragmentQsTogglesBinding>(), QsTogglesAdapter.QsTogglesAdapterCallback {

    // activityViewModels() grabs the ViewModel of the parent activity
    private val mainViewModel: MainViewModel by activityViewModels()
    private val appItemObserver = Observer<Event<Pair<String, AppItemModel>>> { it.getDataIfNotHandled()?.let { (serviceTag, appData) ->
        viewModel.updateToggleItem(serviceTag, appData, true)
    } }

    private val viewModel: QsTogglesViewModel by viewModels()
    private val shortcutListObserver = Observer<List<ShortcutItemModel>> { newList ->
        listAdapter.updateItems(newList)
    }
    private val updateTileObserver = Observer<Pair<String, Boolean>> { (serviceTag, isEnabled) ->
        toggleQuickSettingsTile(serviceTag, isEnabled)
    }

    private val undoRemoveObserver = Observer<Event<ShortcutItemModel>> { it.getDataIfNotHandled()?.let { (serviceTag, appData) ->
        appData?.let { notifyUndoRemove(serviceTag, appData) }
    } }
    private val addedItemObserver = Observer<Event<ShortcutItemModel>> { it.getDataIfNotHandled()?.let { (serviceTag, appData) ->
        notifyAddedItem(serviceTag, appData)
    } }

    private val listAdapter by lazy { QsTogglesAdapter(lifecycleScope) }

    private val serviceTags: List<String> by lazy { arguments?.getStringArray(ARG_SERVICE_TAGS)?.toList() ?: listOf() }

    companion object {
        val TAG: String = this::class.java.name

        private const val ARG_SERVICE_TAGS = "ARG_SERVICE_TAGS"

        fun newInstance(qsServiceTags: List<String>): QsTogglesFragment {
            val bundle = Bundle().apply {
                putStringArray(ARG_SERVICE_TAGS, qsServiceTags.toTypedArray())
            }
            return QsTogglesFragment().apply {
                arguments = bundle
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentQsTogglesBinding {
        return FragmentQsTogglesBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel.shortcutList.observe(this, shortcutListObserver)
        viewModel.updateTile.observe(this, updateTileObserver)

        viewModel.removedItem.observe(this, undoRemoveObserver)
        viewModel.addedItem.observe(this, addedItemObserver)

        context?.packageManager?.let {
            viewModel.packageManager = it
        }

        // Subscribe to parent ViewModel as well
        mainViewModel.selectedItem.observe(this, appItemObserver)
    }

    override fun initView(viewBinding: FragmentQsTogglesBinding) {
        viewBinding.apply {
            recyclerQsToggles.let {
                it.adapter = listAdapter
                listAdapter.setAdapterListener(this@QsTogglesFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.initToggleList(serviceTags)
    }

    override fun onQsAssign(serviceTag: String) {
        mainViewModel.assignPosition(serviceTag)
    }

    override fun onQsClear(serviceTag: String) {
        (parentActivity as? MainActivity)?.toggleBottomSheetFragment(false, AppListFragment.TAG)
        viewModel.updateToggleItem(serviceTag)
    }

    override fun onQsItemClick() {
        (parentActivity as? MainActivity)?.toggleBottomSheetFragment(false, AppListFragment.TAG)
    }

    override fun onQsLongClick(packageName: String) {
        (parentActivity as? MainActivity)?.toggleBottomSheetFragment(false, AppListFragment.TAG)
        val intent = Utils.buildAppDetailsIntent(packageName)
        parentActivity?.runGenericIntent(intent)
    }

    override fun onAppIconClick(appData: AppItemModel) {
        (parentActivity as? MainActivity)?.toggleBottomSheetFragment(true, MonoIconFragment.TAG, appData)
    }

    fun refreshAppIcon(packageName: String) {
        // Update the icon in the adapter
        listAdapter.notifyItemRangeChanged(0, serviceTags.lastIndex, packageName)

        // Update the icon in the TileServices
        viewModel.initToggleList(serviceTags)
    }

    private fun toggleQuickSettingsTile(serviceTag: String, isEnabled: Boolean) {
        context?.apply {
            when (serviceTag) {
                QsService01.TAG -> ComponentName(this, QsService01::class.java)
                QsService02.TAG -> ComponentName(this, QsService02::class.java)
                QsService03.TAG -> ComponentName(this, QsService03::class.java)
                QsService04.TAG -> ComponentName(this, QsService04::class.java)
                QsService05.TAG -> ComponentName(this, QsService05::class.java)
                QsService06.TAG -> ComponentName(this, QsService06::class.java)
                QsService07.TAG -> ComponentName(this, QsService07::class.java)
                QsService08.TAG -> ComponentName(this, QsService08::class.java)
                QsService09.TAG -> ComponentName(this, QsService09::class.java)
                QsService10.TAG -> ComponentName(this, QsService10::class.java)
                QsService11.TAG -> ComponentName(this, QsService11::class.java)
                QsService12.TAG -> ComponentName(this, QsService12::class.java)
                else -> null
            }?.let { componentName ->
                viewModel.packageManager.setComponentEnabledSetting(
                    componentName,
                    if (isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    private fun notifyUndoRemove(serviceTag: String, appData: AppItemModel) {
        (parentActivity as? MainActivity)?.showSnackbar(getString(R.string.shortcut_cleared), Pair(getString(R.string.undo), {
            viewModel.updateToggleItem(serviceTag, appData)
        }))
    }

    private fun notifyAddedItem(serviceTag: String, appData: AppItemModel?) {
        (parentActivity as? MainActivity)?.apply {
            showSnackbar(getString(R.string.shortcut_enabled), Pair(getString(R.string.help), {
                toggleBottomSheetFragment(true, QsHelpFragment.TAG)
            }))
        }

        appData?.packageName?.let { packageName ->
            when (serviceTag) {
                QsService01.TAG -> 0
                QsService02.TAG -> 1
                QsService03.TAG -> 2
                QsService04.TAG -> 3
                QsService05.TAG -> 4
                QsService06.TAG -> 5
                QsService07.TAG -> 6
                QsService08.TAG -> 7
                QsService09.TAG -> 8
                QsService10.TAG -> 9
                QsService11.TAG -> 10
                QsService12.TAG -> 12
                else -> null
            }?.let { adapterPosition ->
                binding?.recyclerQsToggles?.let { recyclerView ->
                    recyclerView.postDelayed({
                        if (!PrefHelper.isTunedIcon(packageName)) onAppIconClick(appData)
                    }, 1500)
                }
            }
        }
    }
}
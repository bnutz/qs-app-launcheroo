package com.justbnutz.quicksettingsapplauncheroo

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.justbnutz.quicksettingsapplauncheroo.databinding.ActivityMainBinding
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.services.*
import com.justbnutz.quicksettingsapplauncheroo.utils.PrefHelper
import com.justbnutz.quicksettingsapplauncheroo.views.AppListFragment
import com.justbnutz.quicksettingsapplauncheroo.views.MonoIconFragment
import com.justbnutz.quicksettingsapplauncheroo.views.QsHelpFragment
import com.justbnutz.quicksettingsapplauncheroo.views.QsTogglesFragment

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    private val assignPositionObserver = Observer<Event<String>> { it.getDataIfNotHandled()?.let {
        toggleBottomSheetFragment(true, AppListFragment.TAG)
    } }

    private var pendingBottomSheet: (() -> Unit)? = null
    private val bottomSheetBehaviour by lazy { BottomSheetBehavior.from(binding.containerBottomsheet) }
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                hideKeyboard(binding.root)
                (getBottomSheetFragment(AppListFragment.TAG) as? AppListFragment)?.binding?.editSearchApps?.text?.clear()
                pendingBottomSheet?.invoke()
                pendingBottomSheet = null
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private val serviceTags = listOf(
        QsService01.TAG,
        QsService02.TAG,
        QsService03.TAG,
        QsService04.TAG,
        QsService05.TAG,
        QsService06.TAG,
        QsService07.TAG,
        QsService08.TAG,
        QsService09.TAG,
        QsService10.TAG,
        QsService11.TAG,
        QsService12.TAG
    )

    private val bottomSheetFragmentTags = listOf(
        AppListFragment.TAG,
        QsHelpFragment.TAG,
        MonoIconFragment.TAG
    )

    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initViewModel() {
        PrefHelper.init(PreferenceManager.getDefaultSharedPreferences(this))

        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel.assignPosition.observe(this, assignPositionObserver)
    }

    override fun initView(viewBinding: ActivityMainBinding) {
        viewBinding.apply {
            supportFragmentManager.apply {
                if (fragments.isEmpty()) commit {
                    setReorderingAllowed(true)
                    add(R.id.container_quicktoggles, QsTogglesFragment.newInstance(serviceTags), QsTogglesFragment.TAG)
                    add(R.id.container_bottomsheet, getBottomSheetFragment(AppListFragment.TAG), AppListFragment.TAG)

                    // Prepare the other helper fragments, but detach them until we need them
                    bottomSheetFragmentTags.filter { it != AppListFragment.TAG }.forEach { fragmentTag ->
                        val bottomSheetFragment = getBottomSheetFragment(fragmentTag)
                        add(R.id.container_bottomsheet, bottomSheetFragment, fragmentTag)
                        detach(bottomSheetFragment)
                    }
                }
            }

            bottomSheetBehaviour.let {
                it.halfExpandedRatio = 0.75f
                it.skipCollapsed = true
                it.isHideable = true
                it.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun getBottomSheetFragment(tag: String): Fragment {
        return supportFragmentManager.findFragmentByTag(tag) ?: when (tag) {
            AppListFragment.TAG -> AppListFragment.newInstance()
            QsHelpFragment.TAG -> QsHelpFragment.newInstance()
            MonoIconFragment.TAG -> MonoIconFragment.newInstance()
            else -> throw Exception("Invalid tag")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_howto -> {
                toggleBottomSheetFragment(true, QsHelpFragment.TAG)
                true
            }
            R.id.mnu_source_code -> {
                runWebLink(getString(R.string.url_open_source))
                true
            }
            R.id.mnu_licences -> {
                val intent = Intent(this, OssLicensesMenuActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.mnu_privacy -> {
                runWebLink(getString(R.string.url_privacy))
                true
            }
            R.id.mnu_play_store -> {
                runWebLink(getString(R.string.url_play_store))
                true
            }
            R.id.mnu_tweet -> {
                runWebLink(getString(R.string.url_tweet))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        bottomSheetBehaviour.addBottomSheetCallback(bottomSheetCallback)
    }

    override fun onPause() {
        bottomSheetBehaviour.removeBottomSheetCallback(bottomSheetCallback)
        super.onPause()
    }

    fun toggleBottomSheetFragment(showBottomSheet: Boolean, fragmentTag: String, appData: AppItemModel? = null) {
        when {
            !showBottomSheet -> {
                // Just close the bottom sheet
                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            }
            bottomSheetBehaviour.state != BottomSheetBehavior.STATE_HIDDEN -> {
                // If bottom sheet already open, store this action and close the sheet (which will re-trigger this action)
                pendingBottomSheet = { toggleBottomSheetFragment(true, fragmentTag, appData) }
                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            }
            else -> {
                // If bottom sheet is closed, swap the fragments and open the sheet
                supportFragmentManager.commit {
                    bottomSheetFragmentTags.filter { it != fragmentTag }.forEach { otherTag ->
                        detach(getBottomSheetFragment(otherTag))
                    }

                    attach(getBottomSheetFragment(fragmentTag))

                    appData?.let {
                        (getBottomSheetFragment(MonoIconFragment.TAG) as? MonoIconFragment)?.initIcon(appData)
                    }

                    binding.containerBottomsheet.post {
                        bottomSheetBehaviour.state = when (fragmentTag) {
                            AppListFragment.TAG -> BottomSheetBehavior.STATE_HALF_EXPANDED
                            QsHelpFragment.TAG,
                            MonoIconFragment.TAG -> BottomSheetBehavior.STATE_EXPANDED
                            else -> throw Exception("Invalid tag")
                        }
                    }
                }
            }
        }
    }

    fun refreshAppIcon(packageName: String) {
        (supportFragmentManager.findFragmentByTag(QsTogglesFragment.TAG) as? QsTogglesFragment)?.refreshAppIcon(packageName)
    }
}
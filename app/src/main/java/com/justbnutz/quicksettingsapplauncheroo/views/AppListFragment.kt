package com.justbnutz.quicksettingsapplauncheroo.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.justbnutz.quicksettingsapplauncheroo.MainActivity
import com.justbnutz.quicksettingsapplauncheroo.MainViewModel
import com.justbnutz.quicksettingsapplauncheroo.R
import com.justbnutz.quicksettingsapplauncheroo.databinding.FragmentApplistBinding
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.utils.Utils

class AppListFragment : BaseFragment<FragmentApplistBinding>(), AppListAdapter.AppListAdapterCallback {

    // activityViewModels() grabs the ViewModel of the parent activity
    private val mainViewModel: MainViewModel by activityViewModels()

    private val viewModel: AppListViewModel by viewModels()
    private val listAdapter by lazy { AppListAdapter(lifecycleScope) }

    private val appListObserver = Observer<List<AppItemModel>> {
        listAdapter.updateItems(it)
        binding?.txtPlaceholder?.let { view ->
            if (it.isNotEmpty()) view.visibility = View.GONE
            else {
                view.visibility = View.VISIBLE
                view.text = getString(R.string.no_apps_found)
            }
        }
    }

    private val errorMsgObserver = Observer<Event<String>> { it.getDataIfNotHandled()?.let { msg ->
        parentActivity?.showToast(getString(R.string.error_message, msg))
    } }

    companion object {
        val TAG: String = this::class.java.name

        fun newInstance() = AppListFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentApplistBinding {
        return FragmentApplistBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel.appList.observe(this, appListObserver)
        viewModel.errorMsg.observe(this, errorMsgObserver)

        context?.let { _context ->
            _context.packageManager?.let {
                viewModel.packageManager = it
            }
        }
    }

    override fun initView(viewBinding: FragmentApplistBinding) {
        viewBinding.apply {
            recyclerApplist.let {
                it.adapter = listAdapter
                listAdapter.setAdapterListener(this@AppListFragment)
            }

            editSearchApps.addTextChangedListener {
                it?.trim()?.toString()?.let { search ->
                    viewModel.getAppList(search)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAppList()
    }

    override fun onItemClick(appData: AppItemModel) {
        mainViewModel.selectedItem(appData)

        (parentActivity as? MainActivity)?.apply {
            binding.containerBottomsheet.let {
                hideKeyboard(it)
                it.postDelayed({
                    toggleBottomSheetFragment(false, TAG)
                }, 350)
            }
        }
    }

    override fun onItemLongClick(packageName: String) {
        val intent = Utils.buildAppDetailsIntent(packageName)
        parentActivity?.runGenericIntent(intent)
    }
}
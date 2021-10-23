package com.justbnutz.quicksettingsapplauncheroo.views

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.justbnutz.quicksettingsapplauncheroo.MainActivity
import com.justbnutz.quicksettingsapplauncheroo.databinding.FragmentMonoIconBinding
import com.justbnutz.quicksettingsapplauncheroo.models.AppItemModel
import com.justbnutz.quicksettingsapplauncheroo.models.Event
import com.justbnutz.quicksettingsapplauncheroo.utils.PrefHelper

class MonoIconFragment : BaseFragment<FragmentMonoIconBinding>() {

    private val viewModel: MonoIconViewModel by viewModels()
    private val monoIconObserver = Observer<Bitmap> { monoIcon ->
        binding?.imgMonoicon?.setImageBitmap(monoIcon)
    }
    private val thresholdObserver = Observer<Event<Int>> { it.getDataIfNotHandled()?.let { threshold ->
        binding?.sliderThreshold?.value = threshold.toFloat()
    } }
    private val invertObserver = Observer<Event<Boolean>> { it.getDataIfNotHandled()?.let { invert ->
        binding?.chkboxInvert?.isChecked = invert
    } }

    companion object {
        val TAG: String = this::class.java.name

        fun newInstance() = MonoIconFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMonoIconBinding {
        return FragmentMonoIconBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {
        // https://developer.android.com/reference/androidx/lifecycle/ViewModel
        viewModel.monoAppIcon.observe(this, monoIconObserver)
        viewModel.monoThreshold.observe(this, thresholdObserver)
        viewModel.monoInvert.observe(this, invertObserver)
    }

    override fun initView(viewBinding: FragmentMonoIconBinding) {
        viewBinding.apply {
            updateControls()
            chkboxInvert.setOnCheckedChangeListener { _, _ ->
                updateIcon()
            }
            sliderThreshold.addOnChangeListener { _, _, _ ->
                updateIcon()
            }
            btnCancelIcon.setOnClickListener {
                (parentActivity as? MainActivity)?.toggleBottomSheetFragment(false, TAG)
            }
            btnSaveIcon.setOnClickListener {
                viewModel.appData?.packageName?.let { packageName ->
                    PrefHelper.setIconThreshold(packageName, sliderThreshold.value.toInt())
                    PrefHelper.setIconInvert(packageName, chkboxInvert.isChecked)
                    (parentActivity as? MainActivity)?.refreshAppIcon(packageName)
                }
                (parentActivity as? MainActivity)?.toggleBottomSheetFragment(false, TAG)
            }
        }
    }

    fun initIcon(appData: AppItemModel) {
        updateControls()
        viewModel.appData = appData
        viewModel.runMonoConversion(
            PrefHelper.getIconThreshold(appData.packageName),
            PrefHelper.getIconInvert(appData.packageName)
        )
    }

    private fun updateControls() {
        binding?.apply {
            root.post {
                viewModel.appData?.let { appData ->
                    sliderThreshold.value = PrefHelper.getIconThreshold(appData.packageName).toFloat()
                    chkboxInvert.isChecked = PrefHelper.getIconInvert(appData.packageName)
                }
            }
        }
    }

    private fun updateIcon() {
        binding?.apply {
            viewModel.runMonoConversion(
                sliderThreshold.value.toInt(),
                chkboxInvert.isChecked
            )
        }
    }
}
package com.justbnutz.quicksettingsapplauncheroo.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.justbnutz.quicksettingsapplauncheroo.BaseActivity

/**
 * Common methods between all fragments go here
 */
abstract class BaseFragment<vBinding: ViewBinding> : Fragment() {

    val parentActivity by lazy { activity as? BaseActivity<*> }

    var binding: vBinding? = null

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): vBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding(inflater, container)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        binding?.let { initView(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    abstract fun initViewModel()

    abstract fun initView(viewBinding: vBinding)
}
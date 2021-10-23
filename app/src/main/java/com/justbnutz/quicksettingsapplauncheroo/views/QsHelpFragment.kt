package com.justbnutz.quicksettingsapplauncheroo.views

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.justbnutz.quicksettingsapplauncheroo.MainActivity
import com.justbnutz.quicksettingsapplauncheroo.databinding.FragmentQsHelpBinding


class QsHelpFragment : BaseFragment<FragmentQsHelpBinding>() {

    companion object {
        val TAG: String = this::class.java.name

        fun newInstance() = QsHelpFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentQsHelpBinding {
        return FragmentQsHelpBinding.inflate(inflater, container, false)
    }

    override fun initViewModel() {}

    override fun initView(viewBinding: FragmentQsHelpBinding) {
        viewBinding.apply {
            context?.let {
                // https://developer.android.com/guide/webapps/load-local-content
                val assetLoader = WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(it))
                    .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(it))
                    .build()

                webviewQsHelp.webViewClient = LocalContentWebViewClient(assetLoader) { uri -> uri?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, link)
                    parentActivity?.runGenericIntent(intent)
                } }
                webviewQsHelp.loadUrl("https://appassets.androidplatform.net/assets/howto.htm")
            }

            btnCloseHelp.setOnClickListener {
                (parentActivity as? MainActivity)?.toggleBottomSheetFragment(false, TAG)
            }
        }
    }

    private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader, private val runUrl: (Uri?) -> Unit) : WebViewClientCompat() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            runUrl.invoke(request.url)
            return true
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            return assetLoader.shouldInterceptRequest(request.url)
        }
    }
}
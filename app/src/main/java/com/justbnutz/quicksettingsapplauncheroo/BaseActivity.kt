package com.justbnutz.quicksettingsapplauncheroo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Common methods between all activities go here
 */
abstract class BaseActivity<vBinding: ViewBinding> : AppCompatActivity() {

    lateinit var binding: vBinding

    abstract fun getViewBinding(): vBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)

        initViewModel()
        initView(binding)
    }

    abstract fun initViewModel()

    abstract fun initView(viewBinding: vBinding)

    fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let {
            view.windowToken?.let { token ->
                it.hideSoftInputFromWindow(token, 0)
            }
        }
    }

    fun showSnackbar(message: String, action: Pair<String, (view: View?) -> Unit>? = null) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        action?.let { (actionLabel, onClick) ->
            snackbar.setAction(actionLabel) {
                onClick.invoke(it)
            }
            snackbar.setActionTextColor(getColor(R.color.accent_light))
        }
        snackbar.show()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun copyText(copyText: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?)?.let {
            try {
                val clip = ClipData.newPlainText("text", copyText)
                it.setPrimaryClip(clip)

                showToast(getString(R.string.item_copied))
            } catch (e: Exception) {
                showToast(getString(R.string.error_message, e.message))
            }
        }
    }

    fun shareText(shareText: String) {
        try {
            val shareTitle = getString(R.string.share_title)
            val intent = Intent(Intent.ACTION_SEND).also {
                it.type = "text/plain"
                it.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                it.putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
            showToast(getString(R.string.error_message, e.message))
        }
    }

    fun runWebLink(webUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            startActivity(intent)
        } catch (e: Exception) {
            showToast(getString(R.string.error_message, e.message))
        }
    }

    fun runGenericIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showToast(getString(R.string.error_message, e.message))
        }
    }
}
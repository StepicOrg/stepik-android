package org.stepik.android.view.in_app_web_view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_in_app_web_view.*
import kotlinx.android.synthetic.main.dialog_in_app_web_view.view.*
import kotlinx.android.synthetic.main.dialog_in_app_web_view.view.containerView
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setTintedNavigationIcon
import org.stepik.android.presentation.in_app_web_view.InAppWebViewView
import ru.nobird.android.view.base.ui.extension.argument

class InAppWebViewDialogFragment : DialogFragment(), InAppWebViewView {
    companion object {
        const val TAG = "InAppWebViewDialogFragment"

        fun newInstance(title: String, url: String): InAppWebViewDialogFragment =
            InAppWebViewDialogFragment().apply {
                this.title = title
                this.url = url
            }
    }

    private var title: String by argument()
    private var url: String by argument()

    private var webView: WebView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater
            .inflate(R.layout.dialog_in_app_web_view, container, false)
            .also { root ->
                if (webView == null) {
                    webView = WebView(requireContext().applicationContext).also {
                        @SuppressLint("SetJavaScriptEnabled")
                        it.settings.javaScriptEnabled = true
                        it.loadUrl(url)
                    }
                }
                webView?.let { root.containerView.addView(it) }
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        centeredToolbarTitle.text = title
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setTintedNavigationIcon(R.drawable.ic_close_dark)
    }

    override fun onDestroyView() {
        containerView.removeView(webView)
        super.onDestroyView()
    }

    override fun onDestroy() {
        webView = null
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
            }
    }

    override fun setState(state: InAppWebViewView.State) {
        TODO("Not yet implemented")
    }
}
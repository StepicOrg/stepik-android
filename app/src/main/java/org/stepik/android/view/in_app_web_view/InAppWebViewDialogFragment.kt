package org.stepik.android.view.in_app_web_view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.dialog_in_app_web_view.*
import kotlinx.android.synthetic.main.dialog_in_app_web_view.view.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.util.setTintedNavigationIcon
import org.stepik.android.presentation.in_app_web_view.InAppWebViewPresenter
import org.stepik.android.presentation.in_app_web_view.InAppWebViewView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class InAppWebViewDialogFragment : DialogFragment(), InAppWebViewView {
    companion object {
        const val TAG = "InAppWebViewDialogFragment"

        fun newInstance(title: String, url: String, isProvideAuth: Boolean = false): InAppWebViewDialogFragment =
            InAppWebViewDialogFragment().apply {
                this.title = title
                this.url = url
                this.isProvideAuth = isProvideAuth
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var inAppWebViewPresenter: InAppWebViewPresenter

    private var title: String by argument()
    private var url: String by argument()
    private var isProvideAuth: Boolean by argument()

    private var webView: WebView? = null

    private lateinit var viewStateDelegate: ViewStateDelegate<InAppWebViewView.State>

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

        injectComponent()
        inAppWebViewPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(InAppWebViewPresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .inAppWebViewComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater
            .inflate(R.layout.dialog_in_app_web_view, container, false)
            .also { root ->
                if (webView == null) {
                    webView = WebView(requireContext().applicationContext).also {
                        it.isVisible = false
                        @SuppressLint("SetJavaScriptEnabled")
                        it.settings.javaScriptEnabled = true
                        it.settings.domStorageEnabled = true
                        it.isSoundEffectsEnabled = false

                        it.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                inAppWebViewPresenter.onSuccess()
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                inAppWebViewPresenter.onError()
                            }
                        }
                    }
                }
                webView?.let { root.containerView.addView(it) }
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<InAppWebViewView.State.Idle>()
        viewStateDelegate.addState<InAppWebViewView.State.WebLoading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<InAppWebViewView.State.LinkLoading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<InAppWebViewView.State.Error>(error)
        viewStateDelegate.addState<InAppWebViewView.State.Success>(webView as View)

        centeredToolbarTitle.text = title
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setTintedNavigationIcon(R.drawable.ic_close_dark)

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }

        setDataToPresenter()
    }

    private fun setDataToPresenter(forceUpdate: Boolean = true) {
        inAppWebViewPresenter.onData(url, isProvideAuth, forceUpdate)
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
            }
        inAppWebViewPresenter.attachView(this)
    }

    override fun setState(state: InAppWebViewView.State) {
        viewStateDelegate.switchState(state)
        if (state is InAppWebViewView.State.WebLoading) {
            webView?.loadUrl(state.url)
        }
    }

    override fun onStop() {
        inAppWebViewPresenter.detachView(this)
        super.onStop()
    }

    override fun onDestroyView() {
        containerView.removeView(webView)
        super.onDestroyView()
    }

    override fun onDestroy() {
        webView = null
        super.onDestroy()
    }
}
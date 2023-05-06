package org.stepik.android.view.in_app_web_view.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_in_app_web_view.*
import kotlinx.android.synthetic.main.dialog_in_app_web_view.view.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.configuration.EndpointResolver
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.setTintedNavigationIcon
import org.stepik.android.presentation.in_app_web_view.InAppWebViewPresenter
import org.stepik.android.presentation.in_app_web_view.InAppWebViewView
import org.stepik.android.view.in_app_web_view.routing.InAppWebViewUrlProcessor
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import java.net.URL
import javax.inject.Inject

class InAppWebViewDialogFragment : DialogFragment(), InAppWebViewView {
    companion object {
        const val TAG = "InAppWebViewDialogFragment"
        const val IN_APP_WEB_VIEW_DIALOG_REQUEST_CODE = 2313

        fun newInstance(title: String, url: String, isProvideAuth: Boolean = false): InAppWebViewDialogFragment =
            InAppWebViewDialogFragment().apply {
                this.title = title
                this.url = url
                this.isProvideAuth = isProvideAuth
            }
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var inAppWebViewUrlProcessor: InAppWebViewUrlProcessor

    @Inject
    internal lateinit var endpointResolver: EndpointResolver

    private val inAppWebViewPresenter: InAppWebViewPresenter by viewModels { viewModelFactory }

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
                        it.webChromeClient = object : WebChromeClient() {
                            override fun onJsConfirm(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: JsResult
                            ): Boolean {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton(R.string.yes) { _, _ ->
                                        result.confirm()
                                    }
                                    .setNegativeButton(R.string.no) { _, _ ->
                                        result.cancel()
                                    }
                                    .setOnDismissListener { result.cancel() }
                                    .show()
                                return true
                            }
                        }
                        it.setDownloadListener { url, _, _, _, _ ->
                            if (isValidForMagicLink(url)) {
                                MagicLinkDialogFragment
                                    .newInstance(url)
                                    .showIfNotExists(childFragmentManager, MagicLinkDialogFragment.TAG)
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(url)
                                startActivity(intent)
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
        centeredToolbar.setNavigationOnClickListener {
            if (showsDialog) {
                dismiss()
            } else {
                activity?.finish()
            }
        }
        centeredToolbar.setTintedNavigationIcon(R.drawable.ic_close_dark)
        centeredToolbar.inflateMenu(R.menu.in_app_web_view_menu)
        centeredToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_external -> {
                    val externalUrl = webView?.url ?: url
                    screenManager.openLinkInWebBrowser(requireContext(), Uri.parse(externalUrl))
                    true
                }
                else ->
                    super.onOptionsItemSelected(menuItem)
            }
        }

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }

        setDataToPresenter()
    }

    private fun setDataToPresenter(forceUpdate: Boolean = true) {
        inAppWebViewPresenter.onData(inAppWebViewUrlProcessor.processInAppWebViewUrl(url), isProvideAuth, forceUpdate)
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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

    override fun onDismiss(dialog: DialogInterface) {
        (activity as? Callback
            ?: parentFragment as? Callback
            ?: targetFragment as? Callback)
            ?.onDismissed()
        super.onDismiss(dialog)
    }

    private fun isValidForMagicLink(url: String): Boolean {
        val (protocol, host) = with(URL(url)) {
            protocol to host
        }
        val baseUrl = getString(R.string.protocol_host_url, protocol, host)
        return baseUrl == endpointResolver.getBaseUrl()
    }

    interface Callback {
        fun onDismissed()
    }
}
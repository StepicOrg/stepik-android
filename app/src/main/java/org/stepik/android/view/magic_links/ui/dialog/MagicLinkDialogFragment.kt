package org.stepik.android.view.magic_links.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.magic_links.MagicLinkPresenter
import org.stepik.android.presentation.magic_links.MagicLinkView
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class MagicLinkDialogFragment : DialogFragment(), MagicLinkView {
    companion object {
        fun newInstance(url: String, handleUrlInParent: Boolean = false): DialogFragment =
            MagicLinkDialogFragment()
                .apply {
                    this.url = url
                    this.returnUrlToParent = handleUrlInParent
                }

        const val TAG = "MagicLinkDialogFragment"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val magicLinkPresenter: MagicLinkPresenter by viewModels { viewModelFactory }

    private var url: String by argument()
    private var returnUrlToParent: Boolean by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.loading)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        magicLinkPresenter.onData(url)
    }

    private fun injectComponent() {
        App.component()
            .magicLinksComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        magicLinkPresenter.attachView(this)
    }

    override fun setState(state: MagicLinkView.State) {
        if (state is MagicLinkView.State.Success) {
            if (returnUrlToParent) {
                (activity as Callback).handleUrl(state.url)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.url)))
            }
            dismiss()
        }
    }

    override fun onStop() {
        magicLinkPresenter.detachView(this)
        super.onStop()
    }

    interface Callback {
        fun handleUrl(url: String)
    }
}
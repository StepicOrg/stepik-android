package org.stepik.android.view.magic_links.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.magic_links.MagicLinkPresenter
import org.stepik.android.presentation.magic_links.MagicLinkView
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class MagicLinkDialogFragment : DialogFragment(), MagicLinkView {
    companion object {
        fun newInstance(url: String): DialogFragment =
            MagicLinkDialogFragment()
                .apply {
                    this.url = url
                }

        const val TAG = "MagicLinkDialogFragment"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var magicLinkPresenter: MagicLinkPresenter

    private var url: String by argument()

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

        magicLinkPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(MagicLinkPresenter::class.java)
        magicLinkPresenter.onData(url)
    }

    private fun injectComponent() {
        App.component()
            .magicLinksComponentBuilder()
            .build()
            .inject(this)
    }

    override fun setState(state: MagicLinkView.State) {
        if (state is MagicLinkView.State.Success) {
            // action view open
            dismiss()
        }
    }
}
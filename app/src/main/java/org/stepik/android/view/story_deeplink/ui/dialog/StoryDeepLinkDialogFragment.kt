package org.stepik.android.view.story_deeplink.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.story_deeplink.StoryDeepLinkPresenter
import org.stepik.android.presentation.story_deeplink.StoryDeepLinkView
import ru.nobird.android.view.base.ui.extension.argument
import timber.log.Timber
import javax.inject.Inject

class StoryDeepLinkDialogFragment : DialogFragment(), StoryDeepLinkView {
    companion object {
        fun newInstance(storyId: Long, deepLinkUrl: String): DialogFragment =
            StoryDeepLinkDialogFragment()
                .apply {
                    this.storyId = storyId
                    this.deepLinkUrl = deepLinkUrl
                }

        const val TAG = "StoryDeepLinkDialogFragment"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val storyDeepLinkPresenter: StoryDeepLinkPresenter by viewModels { viewModelFactory }

    private var storyId: Long by argument()
    private var deepLinkUrl: String by argument()

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
        storyDeepLinkPresenter.onData(storyId)
    }

    private fun injectComponent() {
        App.component()
            .storyDeepLinkComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        storyDeepLinkPresenter.attachView(this)
    }

    override fun setState(state: StoryDeepLinkView.State) {
        if (state is StoryDeepLinkView.State.Success) {
            Timber.d("Success: ${state.story}")
            Timber.d("Story id: $storyId Data string: $deepLinkUrl")
            dismiss()
        }
        if (state is StoryDeepLinkView.State.Error) {
            dismiss()
        }
    }

    override fun onStop() {
        storyDeepLinkPresenter.detachView(this)
        super.onStop()
    }
}
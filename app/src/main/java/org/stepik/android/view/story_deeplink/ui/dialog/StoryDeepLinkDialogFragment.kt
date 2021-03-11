package org.stepik.android.view.story_deeplink.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.features.stories.mapper.toStory
import org.stepic.droid.features.stories.ui.activity.StoriesActivity
import org.stepik.android.presentation.story_deeplink.StoryDeepLinkPresenter
import org.stepik.android.presentation.story_deeplink.StoryDeepLinkView
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import ru.nobird.android.stories.transition.SharedTransitionIntentBuilder
import ru.nobird.android.view.base.ui.extension.argument
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
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val storyDeepLinkPresenter: StoryDeepLinkPresenter by viewModels { viewModelFactory }

    private var storyId: Long by argument()
    private var deepLinkUrl: String by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        return MaterialAlertDialogBuilder(requireContext())
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
            requireContext().startActivity(
                SharedTransitionIntentBuilder.createIntent(
                    requireContext(), StoriesActivity::class.java,
                    CatalogFragment.CATALOG_DEEPLINK_STORY_KEY, 0, arrayListOf(state.story.toStory())
                )
            )
            analytic.reportAmplitudeEvent(
                AmplitudeAnalytic.Stories.STORY_OPENED, mapOf(
                    AmplitudeAnalytic.Stories.Values.STORY_ID to state.story.id,
                    AmplitudeAnalytic.Stories.Values.SOURCE to AmplitudeAnalytic.Stories.Values.Source.DEEPLINK,
                    AmplitudeAnalytic.Stories.Values.DEEPLINK_URL to deepLinkUrl
                )
            )
            dismiss()
        }
        if (state is StoryDeepLinkView.State.Error) {
            Toast.makeText(requireContext(), R.string.story_deeplink_open_fail, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onStop() {
        storyDeepLinkPresenter.detachView(this)
        super.onStop()
    }
}
package org.stepic.droid.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_feedback.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.util.CacheUtil
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.util.createEmailOnlyChooserIntent
import org.stepik.android.presentation.feedback.FeedbackPresenter
import org.stepik.android.presentation.feedback.FeedbackView
import javax.inject.Inject

class FeedbackFragment : FragmentBase(), FeedbackView {
    companion object {
        fun newInstance(): FeedbackFragment {
            val args = Bundle()

            val fragment = FeedbackFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var feedbackPresenter: FeedbackPresenter

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponentNewArch()
        feedbackPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(FeedbackPresenter::class.java)
    }

    override fun onStart() {
        super.onStart()
        feedbackPresenter.attachView(this)
    }

    override fun onStop() {
        feedbackPresenter.detachView(this)
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
    }

    override fun sendTextFeedback(mailTo: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(mailTo))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, CacheUtil.writeReturnInternalStorageFile(requireContext(), "aboutsystem.txt", body))
        }
        startActivity(emailIntent.createEmailOnlyChooserIntent(requireContext(), getString(R.string.email_chooser_title)))
    }

    private fun initButtons() {
        feedbackGoodButton.setOnClickListener {
            if (config.isAppInStore) {
                screenManager.showStoreWithApp(activity)
            } else {
                feedbackPresenter.sendTextFeedback(
                    getString(R.string.feedback_subject),
                    DeviceInfoUtil.getInfosAboutDevice(context, "\n")
                )
            }
        }
        feedbackBadButton.setOnClickListener {
            feedbackPresenter.sendTextFeedback(
                getString(R.string.feedback_subject),
                DeviceInfoUtil.getInfosAboutDevice(context, "\n")
            )
        }
    }
    private fun injectComponentNewArch() {
        App.component()
            .feedbackComponentBuilder()
            .build()
            .inject(this)
    }

    private fun destroyButtons() {
        feedbackGoodButton.setOnClickListener(null)
        feedbackBadButton.setOnClickListener(null)
    }

    override fun onDestroyView() {
        destroyButtons()
        super.onDestroyView()
    }
}
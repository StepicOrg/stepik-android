package org.stepik.android.view.submission.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepik.android.presentation.submission.SubmissionsPresenter
import org.stepik.android.presentation.submission.SubmissionsView
import androidx.recyclerview.widget.LinearLayoutManager
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import kotlinx.android.synthetic.main.dialog_submissions.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.submission.model.SubmissionItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class SubmissionsFragmentDialog : DialogFragment(), SubmissionsView {
    companion object {
        fun newInstance(): DialogFragment =
            SubmissionsFragmentDialog()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var submissionsPresenter: SubmissionsPresenter

    private lateinit var submissionItemAdapter: DefaultDelegateAdapter<SubmissionItem>

    private lateinit var viewStateDelegate: ViewStateDelegate<SubmissionsView.State>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        submissionsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(SubmissionsPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dialog_submissions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()

        submissionItemAdapter = DefaultDelegateAdapter()

        with(recycler) {
            adapter = submissionItemAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun injectComponent() {
        App.component()
            .submissionComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                window.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
            }

        submissionsPresenter.attachView(this)
    }

    override fun onStop() {
        submissionsPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: SubmissionsView.State) {
        when (state) {

        }
    }

    override fun showNetworkError() {

    }
}
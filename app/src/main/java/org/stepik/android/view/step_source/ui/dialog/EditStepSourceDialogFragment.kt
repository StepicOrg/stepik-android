package org.stepik.android.view.step_source.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.dialog_step_source_edit.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.DiscardTextDialogFragment
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.presentation.step_source.EditStepSourcePresenter
import org.stepik.android.presentation.step_source.EditStepSourceView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class EditStepSourceDialogFragment :
    DialogFragment(),
    EditStepSourceView,
    DiscardTextDialogFragment.Callback {

    companion object {
        const val TAG = "ComposeCommentDialogFragment"

        fun newInstance(stepWrapper: StepPersistentWrapper, lessonTitle: String): DialogFragment =
            EditStepSourceDialogFragment()
                .apply {
                    this.stepWrapper = stepWrapper
                    this.lessonTitle = lessonTitle
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var editStepContentPresenter: EditStepSourcePresenter

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonTitle: String by argument()

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                onClose()
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme_FullScreenDialog)

        injectComponent()
        editStepContentPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(EditStepSourcePresenter::class.java)
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepWrapper.step.id)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_step_source_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        centeredToolbarTitle.text = "$lessonTitle - ${stepWrapper.originalStep.position}"
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setNavigationIcon(R.drawable.ic_close_dark)
        centeredToolbar.inflateMenu(R.menu.comment_compose_menu)
        centeredToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.comment_submit) {
                submit()
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            editStepContentPresenter.fetchStepContent(stepWrapper)
        }
        invalidateMenuState()

        stepContentEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                invalidateMenuState()
            }
        })
    }

    private fun invalidateMenuState() {
        centeredToolbar.menu.findItem(R.id.comment_submit)?.isEnabled =
            stepContentEditText.text?.toString() != stepWrapper.originalStep.block?.text
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }

        editStepContentPresenter.attachView(this)
    }

    override fun onStop() {
        editStepContentPresenter.detachView(this)
        super.onStop()
    }

    private fun submit() {
        stepContentEditText.hideKeyboard()
        editStepContentPresenter.changeStepBlockText(stepWrapper, stepContentEditText.text.toString())
    }

    override fun setState(state: EditStepSourceView.State) {
        when (state) {
            EditStepSourceView.State.Loading ->
                ProgressHelper.activate(progressDialogFragment, childFragmentManager, LoadingProgressDialogFragment.TAG)

            is EditStepSourceView.State.StepLoaded ->
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)

            is EditStepSourceView.State.Complete -> {
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)

                (activity as? Callback
                    ?: parentFragment as? Callback
                    ?: targetFragment as? Callback)
                    ?.onStepContentChanged(state.stepWrapper)

                super.dismiss()
            }
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun dismiss() {
        onClose()
    }

    private fun onClose() {
        if (stepContentEditText.text?.toString() == stepWrapper.originalStep.block?.text) {
            super.dismiss()
        } else {
            DiscardTextDialogFragment
                .newInstance()
                .showIfNotExists(childFragmentManager, DiscardTextDialogFragment.TAG)
        }
    }

    override fun onDiscardConfirmed() {
        super.dismiss()
    }

    override fun setStepWrapperInfo(stepWrapper: StepPersistentWrapper) {
        this.stepWrapper = stepWrapper
        stepContentEditText.setText(stepWrapper.originalStep.block?.text)
    }

    interface Callback {
        fun onStepContentChanged(stepWrapper: StepPersistentWrapper)
    }
}
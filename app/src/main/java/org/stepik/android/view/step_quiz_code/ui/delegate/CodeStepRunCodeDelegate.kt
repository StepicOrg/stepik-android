package org.stepik.android.view.step_quiz_code.ui.delegate

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_run_code.view.*
import org.stepic.droid.R
import org.stepic.droid.code.ui.CodeEditorLayout
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.PopupHelper
import org.stepik.android.model.code.UserCodeRun
import org.stepik.android.presentation.step_quiz_code.StepQuizCodeRunPresenter
import org.stepik.android.presentation.step_quiz_code.StepQuizRunCodeView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import ru.nobird.android.view.base.ui.extension.snackbar

class CodeStepRunCodeDelegate(
    runCodeLayout: View,
    private val codeRunPresenter: StepQuizCodeRunPresenter,
    private val fullScreenCodeTabs: TabLayout,
    private val codeLayout: CodeEditorLayout,
    private val context: Context,
    private val stepWrapper: StepPersistentWrapper
) : StepQuizRunCodeView {

    companion object {
        private const val EVALUATION_FRAME_DURATION_MS = 250
        private const val RUN_CODE_TAB = 2
    }

    private val runCodeScrollView = runCodeLayout.dataScrollView
    private val runCodeInputDataTitle = runCodeLayout.inputDataTitle
    private val runCodeInputSamplePicker = runCodeLayout.inputDataSamplePicker
    private val runCodeInputDataSample = runCodeLayout.inputDataSample
    private val runCodeOutputDataSeparator = runCodeLayout.outputSeparator
    private val runCodeOutputDataTitle = runCodeLayout.outputDataTitle
    private val runCodeOutputDataSample = runCodeLayout.outputDataSample
    private val runCodeFeedback = runCodeLayout.runCodeFeedback
    private val runCodeAction = runCodeLayout.runCodeAction
    private val runCodeSpaceOutputDataFillSpace = runCodeLayout.outputDataFillSpace

    var lang: String  = ""

    private var viewStateDelegate: ViewStateDelegate<StepQuizRunCodeView.State> = ViewStateDelegate()

    init {
        viewStateDelegate.addState<StepQuizRunCodeView.State.Idle>(
            runCodeInputDataTitle,
            runCodeInputSamplePicker,
            runCodeInputDataSample
        )
        viewStateDelegate.addState<StepQuizRunCodeView.State.Loading>(
            runCodeInputDataTitle,
            runCodeInputSamplePicker,
            runCodeInputDataSample,
            runCodeFeedback
        )
        viewStateDelegate.addState<StepQuizRunCodeView.State.ConsequentLoading>(
            runCodeInputDataTitle,
            runCodeInputSamplePicker,
            runCodeInputDataSample,
            runCodeFeedback,
            runCodeOutputDataSeparator,
            runCodeOutputDataTitle,
            runCodeOutputDataSample,
            runCodeSpaceOutputDataFillSpace
        )
        viewStateDelegate.addState<StepQuizRunCodeView.State.UserCodeRunLoaded>(
            runCodeInputDataTitle,
            runCodeInputSamplePicker,
            runCodeInputDataSample,
            runCodeOutputDataSeparator,
            runCodeOutputDataTitle,
            runCodeOutputDataSample,
            runCodeSpaceOutputDataFillSpace
        )

        val inputSamples = stepWrapper
            .step
            .block
            ?.options
            ?.samples
            ?.mapIndexed { index, samples -> context.getString(R.string.step_quiz_code_spinner_item, index + 1, samples.first()) }
            ?: emptyList()

        if (inputSamples.isNotEmpty()) {
            runCodeInputDataSample.setText(
                inputSamples
                    .first()
                    .split(":")
                    .last()
                    .trim()
            )
        } else {
            runCodeInputSamplePicker.visibility = View.INVISIBLE
        }

        val popupWindow = ListPopupWindow(context)

        popupWindow.setAdapter(
            ArrayAdapter<String>(
                context,
                R.layout.run_code_spinner_item,
                inputSamples
            )
        )

        popupWindow.setOnItemClickListener { _, _, position, _ ->
            val sampleInput = inputSamples[position]
                .split(":")
                .last()
                .trim()
            runCodeInputDataSample.setText(sampleInput)
            popupWindow.dismiss()
        }

        popupWindow.anchorView = runCodeInputSamplePicker
        popupWindow.width = context.resources.getDimensionPixelSize(R.dimen.step_quiz_full_screen_code_layout_drop_down_width)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        runCodeInputSamplePicker.setOnClickListener { popupWindow.show() }
        runCodeInputSamplePicker.supportCompoundDrawablesTintList =
            ContextCompat.getColorStateList(context, R.color.color_step_quiz_code_samples)

        runCodeAction.supportCompoundDrawablesTintList =
            ContextCompat.getColorStateList(context, R.color.color_step_submit_button_text)

        runCodeAction.setOnClickListener {
            codeRunPresenter.createUserCodeRun(
                code = codeLayout.text.toString(),
                language = lang,
                stdin = runCodeInputDataSample.text.toString(),
                stepId = stepWrapper.step.id
            )
        }

        val evaluationDrawable = AnimationDrawable()
        evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.isOneShot = false

        runCodeFeedback.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
        evaluationDrawable.start()
    }

    override fun setState(state: StepQuizRunCodeView.State) {
        viewStateDelegate.switchState(state)
        val isEnabled = state is StepQuizRunCodeView.State.Idle ||
                (state is StepQuizRunCodeView.State.UserCodeRunLoaded && state.userCodeRun.status != UserCodeRun.Status.EVALUATION)

        runCodeAction.isEnabled = isEnabled
        runCodeInputSamplePicker.isEnabled = isEnabled
        runCodeInputDataSample.isEnabled = isEnabled

        if (state is StepQuizRunCodeView.State.UserCodeRunLoaded) {
            when (state.userCodeRun.status) {
                UserCodeRun.Status.SUCCESS ->
                    setOutputText(state.userCodeRun.stdout)
                UserCodeRun.Status.FAILURE ->
                    setOutputText(state.userCodeRun.stderr)
                else ->
                    Unit
            }
        }
    }

    override fun showNetworkError() {
        runCodeScrollView.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun showRunCodePopup() {
        PopupHelper.showPopupAnchoredToView(
            context,
            fullScreenCodeTabs.getTabAt(RUN_CODE_TAB)?.customView,
            context.getString(R.string.step_quiz_code_run_code_tooltip),
            cancelableOnTouchOutside = true,
            withArrow = true
        )
    }

    private fun setOutputText(text: String?) {
        if (text.isNullOrEmpty()) {
            runCodeOutputDataSample.text = context.getString(R.string.step_quiz_code_empty_output)
        } else {
            runCodeOutputDataSample.text = text
        }
    }
}
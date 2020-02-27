package org.stepik.android.view.step_quiz_code.ui.delegate

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_run_code.view.*
import org.stepic.droid.R
import org.stepic.droid.code.ui.CodeEditorLayout
import org.stepic.droid.model.code.ProgrammingLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.PopupHelper
import org.stepik.android.model.code.UserCodeRun
import org.stepik.android.presentation.step_quiz_code.StepQuizCodeRunPresenter
import org.stepik.android.presentation.step_quiz_code.StepQuizRunCodeView
import org.stepik.android.view.step_quiz_code.model.CodeOutputColors
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import ru.nobird.android.view.base.ui.extension.setTextColorRes
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

    var lang: String  = ""
        set(value) {
            field = value
            if (lang == ProgrammingLanguage.SQL.serverPrintableName) {
                runCodeInputDataSample.setHint(R.string.step_quiz_code_input_not_supported)
            }
        }

    private val viewStateDelegate = ViewStateDelegate<StepQuizRunCodeView.State>()

    init {
        viewStateDelegate.addState<StepQuizRunCodeView.State.Idle>(
            runCodeInputDataTitle,
            runCodeInputDataSample
        )
        viewStateDelegate.addState<StepQuizRunCodeView.State.Loading>(
            runCodeInputDataTitle,
            runCodeInputDataSample,
            runCodeFeedback
        )
        viewStateDelegate.addState<StepQuizRunCodeView.State.ConsequentLoading>(
            runCodeInputDataTitle,
            runCodeInputDataSample,
            runCodeFeedback,
            runCodeOutputDataSeparator,
            runCodeOutputDataTitle,
            runCodeOutputDataSample
        )
        viewStateDelegate.addState<StepQuizRunCodeView.State.UserCodeRunLoaded>(
            runCodeInputDataTitle,
            runCodeInputDataSample,
            runCodeOutputDataSeparator,
            runCodeOutputDataTitle,
            runCodeOutputDataSample
        )

        val samples = stepWrapper
            .step
            .block
            ?.options
            ?.samples
            ?: emptyList()

        val inputSamples = samples
            .mapIndexed { index, sample -> context.getString(R.string.step_quiz_code_spinner_item, index + 1, sample.first()) }

        if (samples.isNotEmpty() && runCodeInputDataSample.text.isNullOrEmpty()) {
            runCodeInputDataSample
                .setText(samples.first().first())
        } else {
            runCodeInputSamplePicker.isGone = true
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
            val sampleInput = samples[position].first()
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
        runCodeInputDataSample.isEnabled = isEnabled && !(lang == ProgrammingLanguage.SQL.serverPrintableName)

        shiftSampleWeights(state)

        when (state) {
            is StepQuizRunCodeView.State.ConsequentLoading ->
                resolveOutputText(state.userCodeRun)
            is StepQuizRunCodeView.State.UserCodeRunLoaded ->
                resolveOutputText(state.userCodeRun)
        }
    }

    override fun showNetworkError() {
        runCodeScrollView.snackbar(messageRes = R.string.connectionProblems) { setTextColorRes(R.color.white) }
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

    override fun setInputData(inputData: String) {
        runCodeInputDataSample.setText(inputData)
    }

    override fun showEmptyCodeError() {
        runCodeScrollView.snackbar(messageRes = R.string.step_quiz_code_empty_code) { setTextColorRes(R.color.white) }
    }

    fun onDetach() {
        codeRunPresenter.saveInputData(runCodeInputDataSample.text.toString())
    }

    private fun resolveOutputText(userCodeRun: UserCodeRun) {
        when (userCodeRun.status) {
            UserCodeRun.Status.SUCCESS -> {
                setOutputTextColor(CodeOutputColors.STANDARD)
                setOutputText(userCodeRun.stdout)
            }
            UserCodeRun.Status.FAILURE -> {
                setOutputTextColor(CodeOutputColors.ERROR)
                if (lang == ProgrammingLanguage.SQL.serverPrintableName) {
                    setOutputText(userCodeRun.stdout)
                } else {
                    setOutputText(userCodeRun.stderr)
                }
            }
            else ->
                return
        }
    }

    private fun setOutputTextColor(codeOutputColors: CodeOutputColors) {
        runCodeOutputDataTitle.setTextColor(ContextCompat.getColor(context, codeOutputColors.titleColor))
        runCodeOutputDataSample.setTextColor(ContextCompat.getColor(context, codeOutputColors.bodyColor))
        runCodeOutputDataTitle.setBackgroundColor(ContextCompat.getColor(context, codeOutputColors.backgroundColor))
        runCodeOutputDataSample.setBackgroundColor(ContextCompat.getColor(context, codeOutputColors.backgroundColor))
    }

    private fun setOutputText(text: String?) {
        runCodeOutputDataSample.text =
            text?.takeIf(String::isNotEmpty)
                ?: context.getString(R.string.step_quiz_code_empty_output)
    }

    private fun shiftSampleWeights(state: StepQuizRunCodeView.State) {
        val (inputDataSampleParams, outputDataSampleParams) =  when (state) {
            is StepQuizRunCodeView.State.Idle, is StepQuizRunCodeView.State.Loading -> {
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f } to
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 0f }
            }
            is StepQuizRunCodeView.State.ConsequentLoading, is StepQuizRunCodeView.State.UserCodeRunLoaded -> {
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { weight = 0f } to
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f }
            }
        }

        runCodeInputDataSample.layoutParams = inputDataSampleParams
        runCodeOutputDataSample.layoutParams = outputDataSampleParams
    }
}
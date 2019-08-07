package org.stepik.android.view.step_quiz_fullscreen_code.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step_quiz_code_fullscreen_instruction.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizDetailsMapper
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailLimitAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailSampleAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class CodeStepQuizFullScreenInstructionFragment : Fragment() {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, currentLang: String): Fragment =
            CodeStepQuizFullScreenInstructionFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.currentLang = currentLang
                }
    }

    private var stepWrapper: StepPersistentWrapper by argument()
    private var latexLayout: LatexSupportableEnhancedFrameLayout? = null
    private var currentLang: String by argument()

    private val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()
    private val codeStepQuizDetailsMapper = CodeStepQuizDetailsMapper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_code_fullscreen_instruction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (latexLayout == null) {
            latexLayout = stepQuizCodeTextContent as LatexSupportableEnhancedFrameLayout

            val text = stepWrapper
                    .step
                    .block
                    ?.text
                    ?.takeIf(String::isNotEmpty)

            stepQuizCodeTextContent.changeVisibility(needShow = text != null)
            if (text != null) {
                stepQuizCodeTextContent.setText(text)
                stepQuizCodeTextContent.setTextIsSelectable(true)
            }
        }

        stepQuizCodeDetailsAdapter += CodeDetailSampleAdapterDelegate()
        stepQuizCodeDetailsAdapter += CodeDetailLimitAdapterDelegate()

        with(stepQuizCodeDetailsContent) {
            layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically(): Boolean =
                    false
            }
            adapter = stepQuizCodeDetailsAdapter

            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.bg_step_quiz_code_details_separator
                )!!
            )
            addItemDecoration(divider)
        }
        stepQuizCodeDetailsContent.isNestedScrollingEnabled = false

        stepQuizCodeDetailsAdapter.items =
            codeStepQuizDetailsMapper.mapToCodeDetails(stepWrapper.step, currentLang)
    }
}
package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepik.android.model.Step
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizDetailsMapper
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailLimitAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailSampleAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CodeQuizInstructionDelegate(
    detailsContainerView: View,
    isCollapseable: Boolean
) {

    private val stepQuizCodeDetails = detailsContainerView.stepQuizCodeDetails
    private val stepQuizCodeDetailsArrow = detailsContainerView.stepQuizCodeDetailsArrow
    private val stepQuizCodeDetailsContent = detailsContainerView.stepQuizCodeDetailsContent

    private val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()
    private val codeStepQuizDetailsMapper = CodeStepQuizDetailsMapper()

    init {
        stepQuizCodeDetailsAdapter += CodeDetailSampleAdapterDelegate()
        stepQuizCodeDetailsAdapter += CodeDetailLimitAdapterDelegate()

        with(stepQuizCodeDetailsContent) {
            layoutManager = LinearLayoutManager(context)
            adapter = stepQuizCodeDetailsAdapter
            isNestedScrollingEnabled = false

            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_step_quiz_code_details_separator)!!)
            addItemDecoration(divider)
        }

        if (isCollapseable) {
            stepQuizCodeDetails.setOnClickListener {
                stepQuizCodeDetailsArrow.changeState()
                if (stepQuizCodeDetailsArrow.isExpanded()) {
                    expand(view = stepQuizCodeDetailsContent)
                } else {
                    collapse(view = stepQuizCodeDetailsContent)
                }
            }
        } else {
            stepQuizCodeDetailsContent.visibility = View.VISIBLE
        }
    }

    fun setCodeDetailsData(step: Step, lang: String?) {
        stepQuizCodeDetailsAdapter.items = codeStepQuizDetailsMapper.mapToCodeDetails(step, lang)
        stepQuizCodeDetails.isVisible = stepQuizCodeDetailsAdapter.items.isNotEmpty()
    }
}
package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailLimitAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailSampleAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class CodeQuizInstructionDelegate(
    detailsContainerView: View,
    private val isCollapseable: Boolean
) {

    val stepQuizCodeDetails = detailsContainerView.stepQuizCodeDetails
    val stepQuizCodeDetailsArrow = detailsContainerView.stepQuizCodeDetailsArrow
    val stepQuizCodeDetailsContent = detailsContainerView.stepQuizCodeDetailsContent

    val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()

    fun setCodeDetailsData(codeDetails: List<CodeDetail>) {
        stepQuizCodeDetailsAdapter.items = codeDetails
    }

    fun setupCodeDetailView() {
        stepQuizCodeDetailsAdapter += CodeDetailSampleAdapterDelegate()
        stepQuizCodeDetailsAdapter += CodeDetailLimitAdapterDelegate()

        with(stepQuizCodeDetailsContent) {
            visibility = View.GONE
            layoutManager = LinearLayoutManager(context)
            adapter = stepQuizCodeDetailsAdapter

            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_step_quiz_code_details_separator)!!)
            addItemDecoration(divider)
        }

        if (isCollapseable) {
            stepQuizCodeDetails.setOnClickListener {
                stepQuizCodeDetailsArrow.changeState()
                if (stepQuizCodeDetailsArrow.isExpanded()) {
                    StepikAnimUtils.expand(stepQuizCodeDetailsContent)
                } else {
                    StepikAnimUtils.collapse(stepQuizCodeDetailsContent)
                }
            }
        } else {
            stepQuizCodeDetailsContent.visibility = View.VISIBLE
            stepQuizCodeDetailsContent.isNestedScrollingEnabled = false
        }
    }
}
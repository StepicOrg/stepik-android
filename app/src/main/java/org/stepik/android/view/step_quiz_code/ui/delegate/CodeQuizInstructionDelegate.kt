package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.databinding.LayoutStepQuizCodeBinding
import org.stepic.droid.model.code.ProgrammingLanguage
import org.stepic.droid.ui.custom.ArrowImageView
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepik.android.model.Step
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizDetailsMapper
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailLimitAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailSampleAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CodeQuizInstructionDelegate private constructor(
    private val stepQuizCodeDetails: View,
    private val stepQuizCodeDetailsContent: RecyclerView,
    private val stepQuizCodeDetailsArrow: ArrowImageView?,
    isCollapseable: Boolean
) {
    constructor(
        codeLayoutBinding: LayoutStepQuizCodeBinding,
        isCollapseable: Boolean
    ) : this(
        codeLayoutBinding.stepQuizCodeDetails,
        codeLayoutBinding.stepQuizCodeDetailsContent,
        codeLayoutBinding.stepQuizCodeDetailsArrow,
        isCollapseable
    )

    constructor(
        detailsContainerView: View,
        isCollapseable: Boolean
    ) : this(
        detailsContainerView.findViewById(R.id.stepQuizCodeDetails),
        detailsContainerView.findViewById(R.id.stepQuizCodeDetailsContent),
        detailsContainerView.findViewById(R.id.stepQuizCodeDetailsArrow),
        isCollapseable
    )

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
            divider.setDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)!!)
            addItemDecoration(divider)
        }

        if (isCollapseable) {
            stepQuizCodeDetails.setOnClickListener {
                val stepQuizCodeDetailsArrow = stepQuizCodeDetailsArrow ?: return@setOnClickListener
                stepQuizCodeDetailsArrow.changeState()
                if (stepQuizCodeDetailsArrow.isExpanded()) {
                    stepQuizCodeDetailsContent.expand()
                } else {
                    stepQuizCodeDetailsContent.collapse()
                }
            }
        } else {
            stepQuizCodeDetailsContent.isVisible = true
        }
    }

    fun setCodeDetailsData(step: Step, lang: String?) {
        if (lang == ProgrammingLanguage.SQL.serverPrintableName) {
            stepQuizCodeDetails.isVisible = false
        } else {
            stepQuizCodeDetailsAdapter.items = codeStepQuizDetailsMapper.mapToCodeDetails(step, lang)
            stepQuizCodeDetails.isVisible = stepQuizCodeDetailsAdapter.items.isNotEmpty()
        }
    }
}

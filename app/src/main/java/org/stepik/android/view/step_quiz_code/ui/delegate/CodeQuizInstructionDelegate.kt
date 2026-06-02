package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.stepic.droid.R
import org.stepic.droid.databinding.LayoutStepQuizCodeBinding
import org.stepic.droid.model.code.ProgrammingLanguage
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

    private val binding = LayoutStepQuizCodeBinding.bind(detailsContainerView)

    private val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()
    private val codeStepQuizDetailsMapper = CodeStepQuizDetailsMapper()

    init {
        stepQuizCodeDetailsAdapter += CodeDetailSampleAdapterDelegate()
        stepQuizCodeDetailsAdapter += CodeDetailLimitAdapterDelegate()

        with(binding.stepQuizCodeDetailsContent) {
            layoutManager = LinearLayoutManager(context)
            adapter = stepQuizCodeDetailsAdapter
            isNestedScrollingEnabled = false

            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)!!)
            addItemDecoration(divider)
        }

        if (isCollapseable) {
            binding.stepQuizCodeDetails.setOnClickListener {
                binding.stepQuizCodeDetailsArrow.changeState()
                if (binding.stepQuizCodeDetailsArrow.isExpanded()) {
                    binding.stepQuizCodeDetailsContent.expand()
                } else {
                    binding.stepQuizCodeDetailsContent.collapse()
                }
            }
        } else {
            binding.stepQuizCodeDetailsContent.isVisible = true
        }
    }

    fun setCodeDetailsData(step: Step, lang: String?) {
        if (lang == ProgrammingLanguage.SQL.serverPrintableName) {
            binding.stepQuizCodeDetails.isVisible = false
        } else {
            stepQuizCodeDetailsAdapter.items = codeStepQuizDetailsMapper.mapToCodeDetails(step, lang)
            binding.stepQuizCodeDetails.isVisible = stepQuizCodeDetailsAdapter.items.isNotEmpty()
        }
    }
}

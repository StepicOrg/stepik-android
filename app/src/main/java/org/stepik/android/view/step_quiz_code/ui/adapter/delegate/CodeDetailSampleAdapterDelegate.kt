package org.stepik.android.view.step_quiz_code.ui.adapter.delegate

import androidx.core.widget.TextViewCompat
import androidx.appcompat.content.res.AppCompatResources
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_step_quiz_code_detail_sample.view.*
import org.stepic.droid.R
import org.stepik.android.view.base.ui.drawable.GravityDrawable
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CodeDetailSampleAdapterDelegate : AdapterDelegate<CodeDetail, DelegateViewHolder<CodeDetail>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CodeDetail> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_code_detail_sample))

    override fun isForViewType(position: Int, data: CodeDetail): Boolean =
        data is CodeDetail.Sample

    private class ViewHolder(root: View) : DelegateViewHolder<CodeDetail>(root) {
        private val title = root.stepQuizCodeDetailSampleTitle
        private val input = root.stepQuizCodeDetailSampleInput
        private val output = root.stepQuizCodeDetailSampleOutput

        init {
            val sampleHeight = context.resources.getDimensionPixelOffset(R.dimen.step_quiz_code_sample_min_height)

            val inputCompoundDrawable = AppCompatResources
                .getDrawable(context, R.drawable.ic_step_quiz_code_sample_input)
                ?.let { GravityDrawable(it, Gravity.TOP, sampleHeight) }
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(input, inputCompoundDrawable, null, null, null)

            val outputCompoundDrawable = AppCompatResources
                .getDrawable(context, R.drawable.ic_step_quiz_code_sample_output)
                ?.let { GravityDrawable(it, Gravity.TOP, sampleHeight) }
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(output, outputCompoundDrawable, null, null, null)
        }

        override fun onBind(data: CodeDetail) {
            data as CodeDetail.Sample

            title.text = context.getString(R.string.step_quiz_code_detail_sample_title, data.position)
            input.text = data.input
            output.text = data.output
        }
    }
}
package org.stepik.android.view.step_quiz_code.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_step_quiz_code_detail_limit.view.*
import org.stepic.droid.R
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CodeDetailLimitAdapterDelegate : AdapterDelegate<CodeDetail, DelegateViewHolder<CodeDetail>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CodeDetail> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_code_detail_limit))

    override fun isForViewType(position: Int, data: CodeDetail): Boolean =
        data is CodeDetail.Limit

    private class ViewHolder(root: View) : DelegateViewHolder<CodeDetail>(root) {
        private val title = root.stepQuizCodeDetailLimitTitle
        private val value = root.stepQuizCodeDetailLimitValue

        override fun onBind(data: CodeDetail) {
            data as CodeDetail.Limit

            when (data.type) {
                CodeDetail.Limit.Type.TIME -> {
                    title.setText(R.string.step_quiz_code_detail_limit_title_time)
                    value.text = context.resources.getQuantityString(R.plurals.time_seconds, data.value, data.value)
                }

                CodeDetail.Limit.Type.MEMORY -> {
                    title.setText(R.string.step_quiz_code_detail_limit_title_memory)
                    value.text = context.getString(R.string.step_quiz_code_detail_limit_value_memory, data.value)
                }
            }
        }
    }
}
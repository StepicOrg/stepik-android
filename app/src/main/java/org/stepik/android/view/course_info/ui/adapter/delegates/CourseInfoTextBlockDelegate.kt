package org.stepik.android.view.course_info.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import kotlinx.android.synthetic.main.view_course_info_text_block.view.*
import org.stepic.droid.R
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.ui.adapter.CourseInfoAdapter
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate

class CourseInfoTextBlockDelegate(
    fontsProvider: FontsProvider
) : AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>() {
    private val lightFontPath = fontsProvider.provideFontPath(FontType.light)

    override fun onCreateViewHolder(parent: ViewGroup): CourseInfoAdapter.ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_text_block))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.WithTitle.TextBlock

    private inner class ViewHolder(root: View) : CourseInfoAdapter.ViewHolderWithTitle(root) {
        private val blockMessage = root.blockMessage

        init {
            blockMessage.setTextSize(14f)
            blockMessage.textView.setLineSpacing(0f, 1.33f)
        }

        override fun onBind(data: CourseInfoItem) {
            super.onBind(data)
            data as CourseInfoItem.WithTitle.TextBlock
            blockMessage.setPlainOrLaTeXTextWithCustomFontColored(data.text, lightFontPath, R.color.new_accent_color, true)
        }
    }
}
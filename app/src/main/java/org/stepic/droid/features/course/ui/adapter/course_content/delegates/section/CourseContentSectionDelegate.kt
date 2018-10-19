package org.stepic.droid.features.course.ui.adapter.course_content.delegates.section

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_content_section.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder

class CourseContentSectionDelegate(
        adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_content_section))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseContentItem.SectionItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val sectionTitle    = root.sectionTitle
        private val sectionPosition = root.sectionPosition
        private val sectionProgress = root.sectionProgress

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.SectionItem) {
                sectionTitle.text = section.title
                sectionPosition.text = section.position.plus(1).toString()
                sectionProgress.progress = progress.nStepsPassed.toFloat() / progress.nSteps.toFloat()
            }
        }
    }
}
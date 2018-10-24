package org.stepic.droid.features.course.ui.adapter.course_content.delegates.section

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_content_section.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.adapter.course_content.CourseContentDateAdapter
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.features.course.ui.model.course_content.CourseContentSectionDate
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import java.util.*

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
        private val sectionTextProgress = root.sectionTextProgress

        private val sectionTimeLineAdapter = CourseContentDateAdapter()

        init {
            with(root.sectionTimeline) {
                adapter = sectionTimeLineAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.SectionItem) {
                sectionTitle.text = section.title
                sectionPosition.text = section.position.toString()
                sectionProgress.progress = progress.nStepsPassed.toFloat() / progress.nSteps.toFloat()
                sectionTextProgress.text = context.resources.getString(R.string.course_content_text_progress,
                        progress.nStepsPassed, progress.nSteps)

                // todo flatten structure
                sectionTimeLineAdapter.dates = listOf(
                        CourseContentSectionDate(R.string.course_content_timeline_begin_date, section.createDate ?: Date()),
                        CourseContentSectionDate(R.string.course_content_timeline_soft_deadline, section.hardDeadline ?: Date()),
                        CourseContentSectionDate(R.string.course_content_timeline_hard_deadline, section.hardDeadline ?: Date()),
                        CourseContentSectionDate(R.string.course_content_timeline_end_date, section.hardDeadline ?: Date())
                )
            }
        }
    }
}
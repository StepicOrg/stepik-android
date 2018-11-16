package org.stepik.android.view.course_content.ui.adapter.delegates.section

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.view_course_content_section.view.*
import org.stepic.droid.R
import org.stepik.android.view.course_content.ui.adapter.CourseContentTimelineAdapter
import org.stepik.android.view.course_content.ui.adapter.decorators.CourseContentTimelineDecorator
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.StartSnapHelper
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
        private val sectionTextProgress   = root.sectionTextProgress
        private val sectionDownloadStatus = root.sectionDownloadStatus

        private val sectionTimeLineAdapter =
            CourseContentTimelineAdapter()

        init {
            with(root.sectionTimeline) {
                adapter = sectionTimeLineAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                StartSnapHelper().attachToRecyclerView(this)
                addItemDecoration(CourseContentTimelineDecorator())

                this@ViewHolder.sectionTitle.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        setPadding(this@ViewHolder.sectionTitle.left, paddingTop, paddingRight, paddingBottom)
                        layoutManager?.scrollToPosition(0)

                        this@ViewHolder.sectionTitle.viewTreeObserver.removeOnPreDrawListener(this)
                        return true
                    }
                })
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.SectionItem) {
                sectionTitle.text = section.title
                sectionPosition.text = section.position.toString()
                sectionProgress.progress = progress.nStepsPassed.toFloat() / progress.nSteps.toFloat()
                sectionTextProgress.text = context.resources.getString(R.string.course_content_text_progress,
                        progress.nStepsPassed, progress.nSteps)
                sectionDownloadStatus.status = downloadStatus

                // todo flatten structure
                sectionTimeLineAdapter.dates = listOf(
                    CourseContentSectionDate(
                        R.string.course_content_timeline_begin_date,
                        section.createDate ?: Date().apply { time -= 100000 }),
                    CourseContentSectionDate(
                        R.string.course_content_timeline_soft_deadline,
                        section.hardDeadline ?: Date().apply { time -= 10000 }),
                    CourseContentSectionDate(
                        R.string.course_content_timeline_hard_deadline,
                        section.hardDeadline ?: Date().apply { time += 10000 }),
                    CourseContentSectionDate(
                        R.string.course_content_timeline_end_date,
                        section.hardDeadline ?: Date().apply { time += 100000 })
                )
            }
        }
    }
}
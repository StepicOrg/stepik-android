package org.stepik.android.view.course_content.ui.adapter.delegates.section

import android.support.v4.util.LongSparseArray
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.view_course_content_section.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.view.course_content.ui.adapter.CourseContentTimelineAdapter
import org.stepik.android.view.course_content.ui.adapter.decorators.CourseContentTimelineDecorator
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.StartSnapHelper
import org.stepic.droid.ui.util.changeVisibility

class CourseContentSectionDelegate(
    adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>,
    private val sectionClickListener: CourseContentSectionClickListener,
    private val sectionDownloadStatuses: LongSparseArray<DownloadProgress.Status>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_content_section))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseContentItem.SectionItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val sectionTitle    = root.sectionTitle
        private val sectionPosition = root.sectionPosition
        private val sectionTimeline = root.sectionTimeline
        private val sectionProgress = root.sectionProgress
        private val sectionTextProgress    = root.sectionTextProgress
        private val sectionDownloadStatus  = root.sectionDownloadStatus
        private val sectionExamDescription = root.sectionExamDescription

        private val sectionTimeLineAdapter =
            CourseContentTimelineAdapter()

        init {
            with(sectionTimeline) {
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

            sectionDownloadStatus.setOnClickListener {
                val item = (itemData as? CourseContentItem.SectionItem) ?: return@setOnClickListener
                when(sectionDownloadStatus.status) {
                    DownloadProgress.Status.NotCached ->
                        sectionClickListener.onItemDownloadClicked(item)

                    DownloadProgress.Status.Cached,
                    is DownloadProgress.Status.InProgress ->
                        sectionClickListener.onItemRemoveClicked(item)
                }
            }

            root.setOnClickListener {
                val item = (itemData as? CourseContentItem.SectionItem) ?: return@setOnClickListener
                sectionClickListener.onItemClicked(item)
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.SectionItem) {
                sectionTitle.text = section.title
                sectionPosition.text = section.position.toString()

                if (progress != null) {
                    sectionProgress.progress = progress.nStepsPassed.toFloat() / progress.nSteps.toFloat()
                    sectionTextProgress.text = context.resources.getString(R.string.course_content_text_progress,
                        progress.nStepsPassed, progress.nSteps)
                    sectionTextProgress.visibility = View.VISIBLE
                } else {
                    sectionProgress.progress = 0f
                    sectionTextProgress.visibility = View.GONE
                }

                sectionDownloadStatus.status = sectionDownloadStatuses[data.section.id] ?: DownloadProgress.Status.Pending
                sectionTimeLineAdapter.dates = dates
                sectionTimeline.changeVisibility(dates.isNotEmpty())

                sectionDownloadStatus.changeVisibility(isEnabled)
                itemView.isEnabled = section.isExam

                val alpha = if (isEnabled) 1f else 0.4f
                sectionTitle.alpha = alpha
                sectionPosition.alpha = alpha
                sectionTimeline.alpha = alpha

                sectionExamDescription.changeVisibility(section.isExam)
            }
        }
    }
}
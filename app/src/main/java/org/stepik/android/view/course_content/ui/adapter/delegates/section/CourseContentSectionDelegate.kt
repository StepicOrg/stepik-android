package org.stepik.android.view.course_content.ui.adapter.delegates.section

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.collection.LongSparseArray
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_course_content_section.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.util.StartSnapHelper
import org.stepic.droid.util.toFixed
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.ui.adapter.CourseContentTimelineAdapter
import org.stepik.android.view.course_content.ui.adapter.decorators.CourseContentTimelineDecorator
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseContentSectionDelegate(
    private val sectionClickListener: CourseContentSectionClickListener,
    private val sectionDownloadStatuses: LongSparseArray<DownloadProgress.Status>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_content_section))

    override fun isForViewType(position: Int, data: CourseContentItem): Boolean =
        data is CourseContentItem.SectionItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val sectionTitle    = root.sectionTitle
        private val sectionPosition = root.sectionPosition
        private val sectionTimeline = root.sectionTimeline
        private val sectionProgress = root.sectionProgress
        private val sectionTextProgress    = root.sectionTextProgress
        private val sectionDownloadStatus  = root.sectionDownloadStatus
        private val sectionExamDescription = root.sectionExamDescription
        private val sectionRequirementsDescription = root.sectionRequirementsDescription

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
                when (sectionDownloadStatus.status) {
                    DownloadProgress.Status.NotCached ->
                        sectionClickListener.onItemDownloadClicked(item)

                    is DownloadProgress.Status.InProgress ->
                        sectionClickListener.onItemCancelClicked(item)

                    is DownloadProgress.Status.Cached ->
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

                if (progress != null && progress.cost > 0) {
                    val score = progress
                        .score
                        ?.toFloatOrNull()
                        ?: 0f

                    sectionProgress.progress = score.toLong() / progress.cost.toFloat()
                    sectionTextProgress.text = context.resources.getString(R.string.course_content_text_progress_points,
                        score.toFixed(2), progress.cost)
                    sectionTextProgress.visibility = View.VISIBLE
                } else {
                    sectionProgress.progress = 0f
                    sectionTextProgress.visibility = View.GONE
                }

                sectionDownloadStatus.status = sectionDownloadStatuses[data.section.id] ?: DownloadProgress.Status.Pending
                sectionTimeLineAdapter.dates = dates
                sectionTimeline.isVisible = dates.isNotEmpty()

                sectionDownloadStatus.isVisible = isEnabled
                itemView.isEnabled = section.isExam

                val alpha = if (isEnabled) 1f else 0.4f
                sectionTitle.alpha = alpha
                sectionPosition.alpha = alpha
                sectionTimeline.alpha = alpha

                sectionExamDescription.isVisible = section.isExam

                if (requiredSection != null) {
                    val requiredPoints = requiredSection.progress.cost * requiredSection.section.requiredPercent / 100

                    sectionRequirementsDescription.text =
                        context.getString(
                            R.string.course_content_section_requirements,
                            context.resources.getQuantityString(R.plurals.points, requiredPoints.toInt(), requiredPoints),
                            requiredSection.section.title
                        )

                    sectionRequirementsDescription.isVisible = true
                } else {
                    sectionRequirementsDescription.isVisible = false
                }
            }
        }
    }
}
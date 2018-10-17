package org.stepic.droid.features.course.ui.adapter.course_content

import android.support.v7.util.DiffUtil
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.control_bar.CourseContentControlBarDelegate
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonClickListener
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonDelegate
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.section.CourseContentSectionDelegate
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepik.android.model.Progress

class CourseContentAdapter(
        lessonClickListener: CourseContentLessonClickListener
) : DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    private val headers = listOf(CourseContentItem.ControlBar)

    private var items: MutableList<CourseContentItem> = mutableListOf()
        set(value) {
            DiffUtil.calculateDiff(CourseContentDiffCallback(field, value)).dispatchUpdatesTo(this)
            field = value
        }

    init {
        addDelegate(CourseContentControlBarDelegate(this))
        addDelegate(CourseContentSectionDelegate(this))
        addDelegate(CourseContentLessonDelegate(this, lessonClickListener))
    }

    fun updateSectionDownloadProgress(downloadProgress: DownloadProgress) {
        val sectionPosition = items.indexOfFirst { it is CourseContentItem.SectionItem && it.section.id == downloadProgress.id }
        val sectionItem = items.getOrNull(sectionPosition) as? CourseContentItem.SectionItem ?: return

        items[sectionPosition] = sectionItem.copy(downloadProgress = downloadProgress)
        notifyItemChanged(sectionPosition)
    }

    fun updateSectionProgress(progress: Progress) {
        val sectionPosition = items.indexOfFirst { it is CourseContentItem.SectionItem && it.progress.id == progress.id }
        val sectionItem = items.getOrNull(sectionPosition) as? CourseContentItem.SectionItem ?: return

        items[sectionPosition] = sectionItem.copy(progress = progress)
        notifyItemChanged(sectionPosition)
    }

    fun updateUnitDonwloadProgress(downloadProgress: DownloadProgress) {
        val unitPosition = items.indexOfFirst { it is CourseContentItem.LessonItem && it.unit.id == downloadProgress.id }
        val unitItem = items.getOrNull(unitPosition) as? CourseContentItem.LessonItem ?: return

        items[unitPosition] = unitItem.copy(downloadProgress = downloadProgress)
        notifyItemChanged(unitPosition)
    }

    fun updateUnitProgress(progress: Progress) {
        val unitPosition = items.indexOfFirst { it is CourseContentItem.LessonItem && it.progress.id == progress.id }
        val unitItem = items.getOrNull(unitPosition) as? CourseContentItem.LessonItem ?: return

        items[unitPosition] = unitItem.copy(progress = progress)
        notifyItemChanged(unitPosition)
    }

    fun setData(items: List<CourseContentItem>) {
        this.items = items.toMutableList()
    }

    override fun getItemAtPosition(position: Int): CourseContentItem =
            headers.getOrNull(position) ?: items[position - headers.size]

    override fun getItemCount(): Int =
            headers.size + items.size
}
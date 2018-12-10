package org.stepik.android.view.course_content.ui.adapter

import android.support.v7.util.DiffUtil
import org.stepik.android.view.course_content.ui.adapter.delegates.control_bar.CourseContentControlBarDelegate
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitClickListener
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitDelegate
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionDelegate
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepik.android.model.Progress
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitPlaceholderDelegate

class CourseContentAdapter(
        unitClickListener: CourseContentUnitClickListener
) : DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    private val headers = listOf(CourseContentItem.ControlBar)

    private var items: MutableList<CourseContentItem> = mutableListOf()
        set(value) {
            DiffUtil.calculateDiff(
                CourseContentDiffCallback(
                    field,
                    value
                )
            ).dispatchUpdatesTo(this)
            field = value
        }

    init {
        addDelegate(CourseContentControlBarDelegate(this))
        addDelegate(CourseContentSectionDelegate(this))
        addDelegate(CourseContentUnitDelegate(this, unitClickListener))
        addDelegate(CourseContentUnitPlaceholderDelegate(this))
    }

    fun updateSectionDownloadProgress(downloadProgress: DownloadProgress) {
        val sectionPosition = items.indexOfFirst { it is CourseContentItem.SectionItem && it.section.id == downloadProgress.id }
        val sectionItem = items.getOrNull(sectionPosition) as? CourseContentItem.SectionItem ?: return

        items[sectionPosition] = sectionItem.copy(downloadStatus = downloadProgress.status)
        notifyItemChanged(sectionPosition)
    }

    fun updateSectionProgress(progress: Progress) {
        val sectionPosition = items.indexOfFirst { it is CourseContentItem.SectionItem && it.progress?.id == progress.id }
        val sectionItem = items.getOrNull(sectionPosition) as? CourseContentItem.SectionItem ?: return

        items[sectionPosition] = sectionItem.copy(progress = progress)
        notifyItemChanged(sectionPosition)
    }

    fun updateUnitDonwloadProgress(downloadProgress: DownloadProgress) {
        val unitPosition = items.indexOfFirst { it is CourseContentItem.UnitItem && it.unit.id == downloadProgress.id }
        val unitItem = items.getOrNull(unitPosition) as? CourseContentItem.UnitItem ?: return

        items[unitPosition] = unitItem.copy(downloadStatus = downloadProgress.status)
        notifyItemChanged(unitPosition)
    }

    fun updateUnitProgress(progress: Progress) {
        val unitPosition = items.indexOfFirst { it is CourseContentItem.UnitItem && it.progress?.id == progress.id }
        val unitItem = items.getOrNull(unitPosition) as? CourseContentItem.UnitItem ?: return

        items[unitPosition] = unitItem.copy(progress = progress)
        notifyItemChanged(unitPosition)
    }

    fun setData(items: List<CourseContentItem>) {
        this.items = items.toMutableList()
    }

    fun getData(): List<CourseContentItem> =
        items

    override fun getItemAtPosition(position: Int): CourseContentItem =
            headers.getOrNull(position) ?: items[position - headers.size]

    override fun getItemCount(): Int =
        if (items.isNotEmpty()) headers.size + items.size else 0
}
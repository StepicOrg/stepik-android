package org.stepik.android.view.course_content.ui.adapter

import android.support.v4.util.LongSparseArray
import android.support.v7.util.DiffUtil
import org.stepik.android.view.course_content.ui.adapter.delegates.control_bar.CourseContentControlBarDelegate
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitClickListener
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitDelegate
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionDelegate
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.ui.adapter.delegates.section.CourseContentSectionClickListener
import org.stepik.android.view.course_content.ui.adapter.delegates.unit.CourseContentUnitPlaceholderDelegate

class CourseContentAdapter(
    sectionClickListener: CourseContentSectionClickListener,
    unitClickListener: CourseContentUnitClickListener
) : DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    private val headers = mutableListOf(CourseContentItem.ControlBar(false, PersonalDeadlinesState.Idle))

    private val sectionDownloadStatuses = LongSparseArray<DownloadProgress.Status>()
    private val unitDownloadStatuses = LongSparseArray<DownloadProgress.Status>()

    var items: List<CourseContentItem> = emptyList()
        set(value) {
            DiffUtil
                .calculateDiff(CourseContentDiffCallback(headers + field, headers + value))
                .dispatchUpdatesTo(this)
            field = value
        }

    init {
        addDelegate(CourseContentControlBarDelegate(this))
        addDelegate(CourseContentSectionDelegate(this, sectionClickListener, sectionDownloadStatuses))
        addDelegate(CourseContentUnitDelegate(this, unitClickListener, unitDownloadStatuses))
        addDelegate(CourseContentUnitPlaceholderDelegate(this))
    }

    fun updateSectionDownloadProgress(downloadProgress: DownloadProgress) {
        val sectionPosition = items
            .takeIf { sectionDownloadStatuses[downloadProgress.id] != downloadProgress.status }
            ?.indexOfFirst { it is CourseContentItem.SectionItem && it.section.id == downloadProgress.id }
            ?.takeIf { it >= 0 }
            ?: return

        sectionDownloadStatuses.append(downloadProgress.id, downloadProgress.status)
        notifyItemChanged(sectionPosition + headers.size)
    }

    fun updateUnitDownloadProgress(downloadProgress: DownloadProgress) {
        val unitPosition = items
            .takeIf { unitDownloadStatuses[downloadProgress.id] != downloadProgress.status }
            ?.indexOfFirst { it is CourseContentItem.UnitItem && it.unit.id == downloadProgress.id }
            ?.takeIf { it >= 0 }
            ?: return

        unitDownloadStatuses.append(downloadProgress.id, downloadProgress.status)
        notifyItemChanged(unitPosition + headers.size)
    }

    fun setControlBar(controlBar: CourseContentItem.ControlBar) {
        headers[0] = controlBar
        notifyItemChanged(0)
    }

    override fun getItemAtPosition(position: Int): CourseContentItem =
            headers.getOrNull(position) ?: items[position - headers.size]

    override fun getItemCount(): Int =
        if (items.isNotEmpty()) headers.size + items.size else 0
}
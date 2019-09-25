package org.stepik.android.view.course_content.ui.adapter.delegates.control_bar

import android.support.v4.util.LongSparseArray
import android.support.v4.widget.CircularProgressDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import kotlinx.android.synthetic.main.view_course_content_control_bar.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.setHeight
import org.stepic.droid.util.TextUtil
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem

class CourseContentControlBarDelegate(
    private val controlBarClickListener: CourseContentControlBarClickListener,
    private val courseDownloadStatuses: LongSparseArray<DownloadProgress.Status>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    companion object {
        private const val SMALLEST_FORMAT_UNIT = 1024 * 1024L // 1 mb
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_content_control_bar))

    override fun isForViewType(position: Int, data: CourseContentItem): Boolean =
        data is CourseContentItem.ControlBar

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {

        private val controlBar = root.controlBar
        private lateinit var status: DownloadProgress.Status
        private lateinit var downloadControl: View
        private lateinit var downloadDrawable: ImageView
        private lateinit var downloadText: TextView

        private val progressDrawable = CircularProgressDrawable(context).apply {
            strokeWidth = 5f
            centerRadius = 20f
            setColorSchemeColors(0x535366)
            start()
        }

        private val controlBarHeight: Int

        init {
            controlBar.onClickListener = { id ->
                val data = (itemData as? CourseContentItem.ControlBar)
                if (data == null) {
                    false
                } else {
                    when (id) {
                        R.id.course_control_schedule ->
                            handleScheduleClick(data)

                        R.id.course_control_download_all -> {
                            if (data.course != null) {
                                when (status) {
                                    is DownloadProgress.Status.NotCached ->
                                        controlBarClickListener.onDownloadAllClicked(data.course)
                                    is DownloadProgress.Status.Cached ->
                                        controlBarClickListener.onRemoveAllClicked(data.course)
                                }
                            }
                        }
                    }
                    true
                }
            }

            val attrs = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            try {
                controlBarHeight = attrs.getDimension(0, 0F).toInt()
            } finally {
                attrs.recycle()
            }
        }

        override fun onBind(data: CourseContentItem) {
            data as CourseContentItem.ControlBar

            val isScheduleVisible =
                with(data.personalDeadlinesState) {
                    this is PersonalDeadlinesState.EmptyDeadlines ||
                            this is PersonalDeadlinesState.Deadlines
                } || data.hasDates

            controlBar.changeItemVisibility(R.id.course_control_schedule, isScheduleVisible)
            controlBar.changeItemVisibility(R.id.course_control_download_all, data.course != null)
            controlBar.setHeight(if (data.isEnabled) controlBarHeight else 0)

            downloadControl = controlBar.findViewById<View>(R.id.course_control_download_all)
            downloadDrawable = downloadControl.findViewById(android.R.id.icon)
            downloadText = downloadControl.findViewById(android.R.id.text1)

            if (data.course != null) {
                status = courseDownloadStatuses[data.course.id] ?: DownloadProgress.Status.Pending
                when (status) {
                    is DownloadProgress.Status.InProgress, DownloadProgress.Status.Pending -> {
                        downloadText.text = context.resources.getString(R.string.course_control_processing)
                        downloadDrawable.setImageDrawable(progressDrawable)
                    }
                    is DownloadProgress.Status.Cached -> {
                        downloadText.text = context.resources.getString(
                            R.string.course_control_downloaded_for_offline,
                            TextUtil.formatBytes((status as DownloadProgress.Status.Cached).bytesTotal, SMALLEST_FORMAT_UNIT)
                        )
                        downloadDrawable.setImageResource(R.drawable.ic_download_remove)
                    }
                    is DownloadProgress.Status.NotCached -> {
                        downloadText.text = context.resources.getString(R.string.course_control_download_all)
                        downloadDrawable.setImageResource(R.drawable.ic_download)
                    }
                }
            }
        }

        private fun handleScheduleClick(data: CourseContentItem.ControlBar) {
            when (data.personalDeadlinesState) {
                PersonalDeadlinesState.EmptyDeadlines ->
                    controlBarClickListener.onCreateScheduleClicked()

                is PersonalDeadlinesState.Deadlines -> {
                    val record = data.personalDeadlinesState.record
                    val anchorView = controlBar.findViewById<View>(R.id.course_control_schedule)
                    val popupMenu = PopupMenu(context, anchorView)
                    popupMenu.inflate(R.menu.course_content_control_bar_schedule_menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_item_deadlines_edit ->
                                controlBarClickListener.onChangeScheduleClicked(record)

                            R.id.menu_item_deadlines_remove ->
                                controlBarClickListener.onRemoveScheduleClicked(record)

                            R.id.menu_item_deadlines_sync ->
                                controlBarClickListener.onExportScheduleClicked()
                        }
                        true
                    }
                    popupMenu.show()
                }

                is PersonalDeadlinesState.NoDeadlinesNeeded -> {
                    val anchorView = controlBar.findViewById<View>(R.id.course_control_schedule)
                    val popupMenu = PopupMenu(context, anchorView)
                    popupMenu.inflate(R.menu.course_content_control_bar_schedule_menu_no_personal)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_item_deadlines_sync ->
                                controlBarClickListener.onExportScheduleClicked()
                        }
                        true
                    }
                    popupMenu.show()
                }
            }
        }
    }
}
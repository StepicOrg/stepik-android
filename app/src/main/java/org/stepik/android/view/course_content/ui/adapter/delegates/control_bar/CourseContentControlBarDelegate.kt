package org.stepik.android.view.course_content.ui.adapter.delegates.control_bar

import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.view_course_content_control_bar.view.*
import org.stepic.droid.R
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.util.setHeight
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState

class CourseContentControlBarDelegate(
    adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>,
    private val controlBarClickListener: CourseContentControlBarClickListener
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
        ViewHolder(createView(parent, R.layout.view_course_content_control_bar))

    override fun isForViewType(position: Int): Boolean =
        getItemAtPosition(position) is CourseContentItem.ControlBar

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val controlBar = root.controlBar

        private val controlBarHeight: Int

        init {
            controlBar.onClickListener = { id ->
                val data = (itemData as? CourseContentItem.ControlBar)
                if (data == null) {
                    false
                } else {
                    when(id) {
                        R.id.course_control_schedule ->
                            handleScheduleClick(data)

                        R.id.course_control_download_all -> {
                            if (data.course != null) {
                                controlBarClickListener.onDownloadAllClicked(data.course)
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
                    this is PersonalDeadlinesState.EmptyDeadlines
                            || this is PersonalDeadlinesState.Deadlines
                } || data.hasDates

            controlBar.changeItemVisibility(R.id.course_control_schedule, isScheduleVisible)
            controlBar.changeItemVisibility(R.id.course_control_download_all, data.course != null)
            controlBar.setHeight(if (data.isEnabled) controlBarHeight else 0)
        }

        private fun handleScheduleClick(data: CourseContentItem.ControlBar) {
            when(data.personalDeadlinesState) {
                PersonalDeadlinesState.EmptyDeadlines ->
                    controlBarClickListener.onCreateScheduleClicked()

                is PersonalDeadlinesState.Deadlines -> {
                    val record = data.personalDeadlinesState.record
                    val anchorView = controlBar.findViewById<View>(R.id.course_control_schedule)
                    val popupMenu = PopupMenu(context, anchorView)
                    popupMenu.inflate(R.menu.course_content_control_bar_schedule_menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when(item.itemId) {
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
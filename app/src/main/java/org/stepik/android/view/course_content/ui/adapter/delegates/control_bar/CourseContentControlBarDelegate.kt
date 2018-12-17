package org.stepik.android.view.course_content.ui.adapter.delegates.control_bar

import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_content_control_bar.view.*
import org.stepic.droid.R
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState

class CourseContentControlBarDelegate(
        adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
        ViewHolder(
            createView(parent, R.layout.view_course_content_control_bar)
        )

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseContentItem.ControlBar

    class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val controlBar = root.controlBar

        init {

        }

        override fun onBind(data: CourseContentItem) {
            data as CourseContentItem.ControlBar

            val isScheduleVisible =
                with(data.personalDeadlinesState) {
                    this is PersonalDeadlinesState.EmptyDeadlines
                            || this is PersonalDeadlinesState.Deadlines
                }

            Log.d("CourseContentControlBar", "$isScheduleVisible, $data")

            controlBar.changeItemVisibility(R.id.course_control_schedule, isScheduleVisible)
        }
    }
}
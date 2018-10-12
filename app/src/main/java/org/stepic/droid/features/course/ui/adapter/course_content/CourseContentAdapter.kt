package org.stepic.droid.features.course.ui.adapter.course_content

import org.stepic.droid.features.course.ui.adapter.course_content.delegates.control_bar.CourseContentControlBarDelegate
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonClickListener
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonDelegate
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter

class CourseContentAdapter(
        lessonClickListener: CourseContentLessonClickListener
) : DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    init {
        addDelegate(CourseContentControlBarDelegate(this))
        addDelegate(CourseContentLessonDelegate(this, lessonClickListener))
    }

    override fun getItemAtPosition(position: Int): CourseContentItem =
            CourseContentItem.ControlBar

    override fun getItemCount(): Int = 1
}
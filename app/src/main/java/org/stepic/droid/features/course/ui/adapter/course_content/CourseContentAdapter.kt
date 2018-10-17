package org.stepic.droid.features.course.ui.adapter.course_content

import android.support.v4.util.LongSparseArray
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.control_bar.CourseContentControlBarDelegate
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonClickListener
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson.CourseContentLessonDelegate
import org.stepic.droid.features.course.ui.adapter.course_content.delegates.section.CourseContentSectionDelegate
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

class CourseContentAdapter(
        lessonClickListener: CourseContentLessonClickListener
) : DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    private var sections: List<Section> = emptyList()
    private var units: LongSparseArray<Unit> = LongSparseArray()
    private var lessons: LongSparseArray<Lesson> = LongSparseArray()

    init {
        addDelegate(CourseContentControlBarDelegate(this))
        addDelegate(CourseContentSectionDelegate(this))
        addDelegate(CourseContentLessonDelegate(this, lessonClickListener))
    }

    override fun getItemAtPosition(position: Int): CourseContentItem =
//            if (position == 0) {
                CourseContentItem.ControlBar
//            } else {
//                var currentPosition = position - 1
//
//            }

    override fun getItemCount(): Int =
            sections.sumBy { it.units.size } + sections.size + 1
}
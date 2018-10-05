package org.stepic.droid.features.course.ui.adapter.course_content

sealed class CourseContentAdapterItem {
    object ControlBar : CourseContentAdapterItem()
    class Section() : CourseContentAdapterItem()
    class Unit() : CourseContentAdapterItem()
}
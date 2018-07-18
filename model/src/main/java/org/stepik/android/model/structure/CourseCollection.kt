package org.stepik.android.model.structure

class CourseCollection(
        val id: Long,
        val position: Int,
        val title: String,
        val language: String,
        val courses: LongArray,
        val description: String
)
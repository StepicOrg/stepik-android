package org.stepik.android.data.personal_deadlines

fun getKindOfRecord(courseId: Long): String =
    "deadline_$courseId"
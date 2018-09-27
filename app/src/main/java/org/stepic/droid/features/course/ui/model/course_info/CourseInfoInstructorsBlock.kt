package org.stepic.droid.features.course.ui.model.course_info

import org.stepik.android.model.user.User

class CourseInfoInstructorsBlock(
        val instructors: List<User>
): CourseInfoBlock(CourseInfoType.INSTRUCTORS)
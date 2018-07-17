package org.stepic.droid.util

import org.stepic.droid.model.Course
import org.stepic.droid.model.CourseReviewSummary
import org.stepik.android.model.structure.Progress

object CourseUtil {
     fun applyProgressesToCourses(progresses: Map<String?, Progress>, courses: List<Course>) {
        courses.forEach { course ->
            progresses[course.progress]?.let {
                course.progressObject = it
            }
        }
    }

     fun applyReviewsToCourses(reviews: List<CourseReviewSummary>?, courses: List<Course>) {
        val courseMap = courses.associateBy { it.courseId }
        reviews?.forEach { review ->
            courseMap[review.course]
                    ?.let {
                        it.rating = review.average
                    }
        }
    }
}

package org.stepik.android.domain.course_list.interactor

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.model.UserCourse
import javax.inject.Inject

class CourseListUserInteractor
@Inject
constructor(
    private val userCoursesRepository: UserCoursesRepository,
    private val courseListInteractor: CourseListInteractor
) {
    fun getUserCourses(page: Int = 1): Single<PagedList<CourseListItem.Data>> =
        userCoursesRepository
            .getUserCourses(page = page, sourceType = DataSourceType.REMOTE)
            .flatMap { userCourses ->
                courseListInteractor
                    .getCourseListItems(*userCourses.mapToLongArray(UserCourse::course))
                    .map { courseListItems ->
                        PagedList(
                            list = courseListItems,
                            page = userCourses.page,
                            hasPrev = userCourses.hasPrev,
                            hasNext = userCourses.hasNext
                        )
                    }
            }
}
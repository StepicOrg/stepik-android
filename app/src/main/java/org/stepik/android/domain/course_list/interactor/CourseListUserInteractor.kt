package org.stepik.android.domain.course_list.interactor

import io.reactivex.Observable
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

    fun getAllUserCourses(): Single<List<UserCourse>> =
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { userCoursesRepository.getUserCourses(page = it) }
            .takeUntil { !it.hasNext }
            .reduce(emptyList()) { a, b -> a + b }

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

    fun getUserCourse(courseId: Long): Single<CourseListItem.Data> =
        courseListInteractor
            .getCourseListItems(courseId)
            .map { it.first() }
}
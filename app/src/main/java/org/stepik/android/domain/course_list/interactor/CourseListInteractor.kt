package org.stepik.android.domain.course_list.interactor

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.configuration.RemoteConfig
import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.interactor.CourseStatsInteractor
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.course_purchase.model.CoursePurchaseFlow
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListInteractor
@Inject
constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val courseRepository: CourseRepository,
    private val courseStatsInteractor: CourseStatsInteractor
) {

    fun getAllCourses(courseListQuery: CourseListQuery): Single<List<Course>> =
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { courseRepository.getCourses(courseListQuery.copy(page = it), allowFallback = false) }
            .takeUntil { !it.hasNext }
            .reduce(emptyList()) { a, b -> a + b }

    fun getCourseListItems(
        courseIds: List<Long>,
        courseViewSource: CourseViewSource,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE
    ): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(
            coursesSource = courseRepository.getCourses(courseIds, primarySourceType = sourceTypeComposition.generalSourceType),
            courseViewSource = courseViewSource,
            sourceTypeComposition = sourceTypeComposition
        )

    fun getCourseListItems(
        courseListQuery: CourseListQuery,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE,
        isAllowFallback: Boolean = true
    ): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(
            coursesSource = courseRepository.getCourses(courseListQuery, primarySourceType = sourceTypeComposition.generalSourceType, allowFallback = isAllowFallback),
            courseViewSource = CourseViewSource.Query(courseListQuery),
            sourceTypeComposition = sourceTypeComposition
        )

    private fun getCourseListItems(
        coursesSource: Single<PagedList<Course>>,
        courseViewSource: CourseViewSource,
        sourceTypeComposition: SourceTypeComposition
    ): Single<PagedList<CourseListItem.Data>> =
        coursesSource.flatMap { obtainCourseListItem(it, courseViewSource, sourceTypeComposition) }

    private fun obtainCourseListItem(
        courses: PagedList<Course>,
        courseViewSource: CourseViewSource,
        sourceTypeComposition: SourceTypeComposition
    ): Single<PagedList<CourseListItem.Data>> {
        val currentFlow = CoursePurchaseFlow.valueOfWithFallback(
            firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID]
                .asString()
                .uppercase()
        )

        val isInAppActive = currentFlow.isInAppActive()

        return if (isInAppActive) {
            mapCourseStats(
                courseStatsInteractor.getCourseStatsMobileTiers(courses, sourceTypeComposition, resolveEnrollmentState = false),
                courses,
                courseViewSource
            )
        } else {
            mapCourseStats(
                courseStatsInteractor.getCourseStats(courses, resolveEnrollmentState = false, sourceTypeComposition = sourceTypeComposition),
                courses,
                courseViewSource
            )
        }
    }

    private fun mapCourseStats(courseStatsSource: Single<List<CourseStats>>, courses: PagedList<Course>, courseViewSource: CourseViewSource): Single<PagedList<CourseListItem.Data>> =
        courseStatsSource
            .map { courseStats ->
                val list = courses.mapIndexed { index, course ->
                    CourseListItem.Data(
                        course = course,
                        courseStats = courseStats[index],
                        isAdaptive = adaptiveCoursesResolver.isAdaptive(course.id),
                        source = courseViewSource
                    )
                }
                PagedList(
                    list = list,
                    page = courses.page,
                    hasNext = courses.hasNext,
                    hasPrev = courses.hasPrev
                )
            }
}
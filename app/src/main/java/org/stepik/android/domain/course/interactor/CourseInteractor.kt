package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.model.PromoCode
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.CourseScope
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val solutionsInteractor: SolutionsInteractor,
    private val coursePublishSubject: BehaviorSubject<Course>,
    private val courseStatsInteractor: CourseStatsInteractor
) {
    companion object {
//        private const val COURSE_TIER_PREFIX = "course_tier_"
    }

    fun getCourseHeaderData(courseId: Long, promo: String? = null, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(courseId, if (canUseCache) DataSourceType.CACHE else DataSourceType.REMOTE, canUseCache)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap { obtainCourseHeaderData(it, promo) }

    /**
     * Trying to fetch DB data in first place as course object passed with intent could be obsolete
     */
    fun getCourseHeaderData(course: Course, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(course.id, if (canUseCache) DataSourceType.CACHE else DataSourceType.REMOTE, canUseCache)
            .onErrorReturnItem(course)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap(::obtainCourseHeaderData)

    private fun obtainCourseHeaderData(course: Course, promo: String? = null): Maybe<CourseHeaderData> =
        zip(
            courseStatsInteractor.getCourseStats(listOf(course)),
            solutionsInteractor.fetchAttemptCacheItems(course.id, localOnly = true),
            if (promo == null) Single.just(PromoCode(-1L, "")) else courseStatsInteractor.checkPromoCodeValidity(course.id, promo)
        ) { courseStats, localSubmissions, promoCode ->
            // TODO Handle promoCode
            CourseHeaderData(
                courseId = course.id,
                course = course,
                title = course.title ?: "",
                cover = course.cover ?: "",

                stats = courseStats.first(),
                localSubmissionsCount = localSubmissions.count { it is SolutionItem.SubmissionItem }
            )
        }
            .toMaybe()
}
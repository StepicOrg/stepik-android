package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Progress
import javax.inject.Inject

@CourseListScope
class CourseCollectionPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val courseReviewSummaryRepository: CourseReviewSummaryRepository,
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : PresenterBase<CoursesView>() {

    private val compositeDisposable = CompositeDisposable()

    fun onShowCollections(courseIds: LongArray) {
        view?.showLoading()
        compositeDisposable += courseRepository.getCourses(*courseIds, primarySourceType = DataSourceType.REMOTE)
            .flatMap {
                val progressIds = it.getProgresses()
                val reviewIds = it.map(Course::reviewSummary).toLongArray()

                zip(Single.just(it), getProgressesSingle(progressIds), getReviewsSingle(reviewIds)) { courses, progressMap, reviews ->
                    courses
                }
            }
            .map { courseList ->
                val coursesMap = courseList.associateBy(Course::id)
                courseIds
                    .asIterable()
                    .mapNotNull(coursesMap::get)
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { view?.showCourses(it) },
                onError = { view?.showConnectionProblem() }
            )
    }

    private fun getReviewsSingle(reviewIds: LongArray): Single<List<CourseReviewSummary>> =
        courseReviewSummaryRepository.getCourseReviewSummaries(*reviewIds)
            .subscribeOn(backgroundScheduler)

    private fun getProgressesSingle(progressIds: Array<String>): Single<Map<String?, Progress>> =
        progressRepository.getProgresses(*progressIds)
            .map { it.associateBy(Progress::id) }
            .subscribeOn(backgroundScheduler)

    override fun detachView(view: CoursesView) {
        super.detachView(view)
        compositeDisposable.clear()
    }
}

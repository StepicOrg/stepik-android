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
import org.stepic.droid.util.CourseUtil
import org.stepic.droid.web.Api
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Progress
import org.stepik.android.remote.course.model.CourseResponse
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse
import org.stepik.android.remote.progress.model.ProgressResponse
import javax.inject.Inject

@CourseListScope
class CourseCollectionPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val api: Api
) : PresenterBase<CoursesView>() {

    companion object {
        //collections are small (less than 10 courses), so pagination is not needed
        private const val DEFAULT_PAGE = 1
    }

    private val compositeDisposable = CompositeDisposable()

    fun onShowCollections(courseIds: LongArray) {
        view?.showLoading()
        compositeDisposable += api.getCoursesReactive(DEFAULT_PAGE, courseIds)
            .map(CourseResponse::courses)
            .flatMap {
                val progressIds = it.map(Course::progress).toTypedArray()
                val reviewIds = it.map(Course::reviewSummary).toLongArray()

                zip(Single.just(it), getProgressesSingle(progressIds), getReviewsSingle(reviewIds)) { courses, progressMap, reviews ->
                    CourseUtil.applyProgressesToCourses(progressMap, courses)
                    CourseUtil.applyReviewsToCourses(reviews, courses)
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
        api.getCourseReviewSummaries(reviewIds)
            .map(CourseReviewSummaryResponse::courseReviewSummaries)
            .subscribeOn(backgroundScheduler)

    private fun getProgressesSingle(progressIds: Array<String?>): Single<Map<String?, Progress>> =
        api.getProgressesReactive(progressIds)
            .map(ProgressResponse::progresses)
            .map { it.associateBy(Progress::id) }
            .subscribeOn(backgroundScheduler)

    override fun detachView(view: CoursesView) {
        super.detachView(view)
        compositeDisposable.clear()
    }
}

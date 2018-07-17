package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.model.structure.Course
import org.stepic.droid.model.CourseReviewSummary
import org.stepik.android.model.structure.Progress
import org.stepic.droid.util.CourseUtil
import org.stepic.droid.web.Api
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
        private val DEFAULT_PAGE = 1
    }

    private val compositeDisposable = CompositeDisposable()

    fun onShowCollections(courseIds: LongArray) {
        view?.showLoading()
        val disposable = api
                .getCoursesReactive(DEFAULT_PAGE, courseIds)
                .map { it.courses }
                .flatMap {
                    val progressIds = it.map { it.progress }.toTypedArray()
                    val reviewIds = it.map { it.reviewSummary }.toIntArray()

                    Single.zip(
                            Single.just(it),
                            getProgressesSingle(progressIds),
                            getReviewsSingle(reviewIds),
                            Function3<List<Course>, Map<String?, Progress>, List<CourseReviewSummary>, List<Course>> { courses, progressMap, reviews ->
                                CourseUtil.applyProgressesToCourses(progressMap, courses)
                                CourseUtil.applyReviewsToCourses(reviews, courses)
                                courses
                            })
                }
                .map {
                    val coursesMap = it.associateBy { it.id }
                    courseIds
                            .asIterable()
                            .mapNotNull {
                                coursesMap[it]
                            }
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    view?.showCourses(it)
                }, {
                    view?.showConnectionProblem()
                })

        compositeDisposable.add(disposable)
    }

    private fun getReviewsSingle(reviewIds: IntArray): Single<List<CourseReviewSummary>> {
        return api.getCourseReviews(reviewIds)
                .map {
                    it.courseReviewSummaries
                }
                .subscribeOn(backgroundScheduler)
    }


    private fun getProgressesSingle(progressIds: Array<String?>): Single<Map<String?, Progress>> {
        return api.getProgressesReactive(progressIds)
                .map {
                    it.progresses
                }
                .map { it.associateBy { it.id } }
                .subscribeOn(backgroundScheduler)
    }

    override fun detachView(view: CoursesView) {
        super.detachView(view)
        compositeDisposable.clear()
    }

}

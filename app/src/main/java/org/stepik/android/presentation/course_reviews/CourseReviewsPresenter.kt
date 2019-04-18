package org.stepik.android.presentation.course_reviews

import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.concatWithPagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.interactor.ComposeCourseReviewInteractor
import org.stepik.android.domain.course_reviews.interactor.CourseReviewsInteractor
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.course_reviews.mapper.CourseReviewsStateMapper
import javax.inject.Inject

class CourseReviewsPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,

    private val analytic: Analytic,

    private val courseReviewsInteractor: CourseReviewsInteractor,
    private val composeCourseReviewInteractor: ComposeCourseReviewInteractor,
    private val courseReviewsStateMapper: CourseReviewsStateMapper,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CourseReviewsView>() {

    private var state: CourseReviewsView.State = CourseReviewsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val paginationDisposable = CompositeDisposable()

    init {
        compositeDisposable += paginationDisposable

        subscribeForForceUpdates()
        fetchCourseReviews()
    }

    override fun attachView(view: CourseReviewsView) {
        super.attachView(view)
        view.setState(state)
    }

    /**
     * Listen for swipe to refresh
     */
    private fun subscribeForForceUpdates() {
        compositeDisposable += courseReviewsInteractor
            .getCourseUpdates()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy { forceUpdate() }
    }

    /**
     * Data init variants
     */
    private fun fetchCourseReviews() {
        if (state != CourseReviewsView.State.Idle) return

        state = CourseReviewsView.State.Loading
        paginationDisposable += fetchReviewsFromCache()
            .switchIfEmpty(fetchReviewsFromRemote())
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = it },
                onError   = { state = CourseReviewsView.State.NetworkError }
            )
    }

    private fun fetchReviewsFromCache(): Maybe<CourseReviewsView.State> =
        courseReviewsInteractor
            .getCourseReviewItems(courseId, page = 1, sourceType = DataSourceType.CACHE)
            .filter(List<CourseReviewItem>::isNotEmpty)
            .map(CourseReviewsView.State::CourseReviewsCache)

    private fun fetchReviewsFromRemote(): Single<CourseReviewsView.State> =
        courseReviewsInteractor
            .getCourseReviewItems(courseId, page = 1, sourceType = DataSourceType.REMOTE)
            .map { reviews ->
                if (reviews.isEmpty()) {
                    CourseReviewsView.State.EmptyContent
                } else {
                    CourseReviewsView.State.CourseReviewsRemote(reviews)
                }
            }

    /**
     * Pagination handling
     */
    fun fetchNextPageFromRemote() {
        val oldState = state

        val oldItems = (oldState as? CourseReviewsView.State.CourseReviewsRemote)?.courseReviewItems
            ?: (oldState as? CourseReviewsView.State.CourseReviewsCache)?.courseReviewItems
            ?: return

        val currentItems =
            when {
                oldState is CourseReviewsView.State.CourseReviewsRemote
                        && oldState.courseReviewItems.hasNext ->
                    oldState.courseReviewItems

                oldState is CourseReviewsView.State.CourseReviewsCache ->
                    emptyList<CourseReviewItem>()

                else -> return
            }

        val nextPage = (currentItems as? PagedList<CourseReviewItem>)
            ?.page
            ?.plus(1)
            ?: 1

        state = CourseReviewsView.State.CourseReviewsRemoteLoading(oldItems)
        paginationDisposable += courseReviewsInteractor
            .getCourseReviewItems(courseId, page = nextPage, sourceType = DataSourceType.REMOTE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    state = CourseReviewsView.State.CourseReviewsRemote(currentItems.concatWithPagedList(it))
                    if (oldState is CourseReviewsView.State.CourseReviewsCache) {
                        fetchNextPageFromRemote() // load 2 page from remote after going online
                    }
                },
                onError = { state = oldState; view?.showNetworkError() }
            )
    }

    /**
     * On swipe to refresh
     */
    private fun forceUpdate() {
        paginationDisposable.clear()

        val oldState = state

        state = CourseReviewsView.State.Loading
        paginationDisposable += courseReviewsInteractor
            .getCourseReviewItems(courseId, page = 1, sourceType = DataSourceType.REMOTE)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { reviews ->
                    state =
                        if (reviews.isEmpty()) {
                            CourseReviewsView.State.EmptyContent
                        } else {
                            CourseReviewsView.State.CourseReviewsRemote(reviews)
                        }
                },
                onError = {
                    when (oldState) {
                        is CourseReviewsView.State.CourseReviewsCache,
                        is CourseReviewsView.State.CourseReviewsRemote -> {
                            state = oldState
                            view?.showNetworkError()
                        }

                        is CourseReviewsView.State.CourseReviewsRemoteLoading ->
                            state = CourseReviewsView.State.CourseReviewsRemote(oldState.courseReviewItems)

                        else ->
                            state = CourseReviewsView.State.NetworkError
                    }
                }
            )
    }

    /**
     * Composing course review
     */
    fun removeCourseReview(courseReview: CourseReview) {
        paginationDisposable += composeCourseReviewInteractor
            .removeCourseReview(courseReview.id)
            .andThen(courseReviewsInteractor.resolveCurrentUserCourseReview(courseReview.user, courseReview.course))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSuccess {
                analytic
                    .reportAmplitudeEvent(
                        AmplitudeAnalytic.CourseReview.REVIEW_REMOVED,
                        mapOf(
                            AmplitudeAnalytic.CourseReview.Params.COURSE to courseReview.course,
                            AmplitudeAnalytic.CourseReview.Params.RATING to courseReview.score
                        )
                    )
            }
            .subscribeBy(
                onSuccess = { state = courseReviewsStateMapper.mergeStateWithCurrentUserReview(it, state) },
                onError = { view?.showNetworkError() }
            )
    }

    fun onCourseReviewChanged(courseReview: CourseReview) {
        val items =
            when (val state = state) {
                is CourseReviewsView.State.CourseReviewsCache ->
                    state.courseReviewItems

                is CourseReviewsView.State.CourseReviewsRemote ->
                    state.courseReviewItems

                is CourseReviewsView.State.CourseReviewsRemoteLoading ->
                    state.courseReviewItems

                else ->
                    return
            }

        val item = items
            .find { courseReviewItem ->
                courseReviewItem is CourseReviewItem.Data &&
                courseReviewItem.courseReview.id == courseReview.id
            }
            as? CourseReviewItem.Data
            ?: return


        state = courseReviewsStateMapper
            .mergeStateWithCurrentUserReview(listOf(item.copy(courseReview = courseReview)), state)
    }
}
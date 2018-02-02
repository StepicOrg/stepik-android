package org.stepic.droid.core.presenters

import android.content.Context
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.adaptive.model.RatingItem
import org.stepic.droid.adaptive.ui.adapters.AdaptiveRatingAdapter
import org.stepic.droid.adaptive.util.RatingNamesGenerator
import org.stepic.droid.core.presenters.contracts.AdaptiveRatingView
import org.stepic.droid.di.adaptive.AdaptiveCourseScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import retrofit2.HttpException
import javax.inject.Inject

@AdaptiveCourseScope
class AdaptiveRatingPresenter
@Inject
constructor(
        @CourseId
        private val courseId: Long,
        private val api: Api,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,
        private val ratingNamesGenerator: RatingNamesGenerator,

        context: Context,
        sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<AdaptiveRatingView>() {
    companion object {
        private const val ITEMS_PER_PAGE = 10

        @JvmStatic
        private val RATING_PERIODS = arrayOf(1, 7, 0)
    }

    private val adapters = RATING_PERIODS.map { AdaptiveRatingAdapter(context, sharedPreferenceHelper) }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Int>()

    private var error: Throwable? = null

    private var ratingPeriod = 0

    private var periodsLoaded = 0

    init {
        initRatingPeriods()
    }

    private fun initRatingPeriods() {
        val left = BiFunction<Any, Any, Any> { a, _ -> a}

        RATING_PERIODS.forEachIndexed { pos, period ->
            compositeDisposable.add(api.getRating(courseId, ITEMS_PER_PAGE, period)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .doOnError(this::onError)
                    .retryWhen { x -> x.zipWith(retrySubject, left) }
                    .map { it.users }
                    .subscribe({
                        adapters[pos].set(prepareRatingItems(it))
                        periodsLoaded++
                        onLoadComplete()
                    }, this::onError))
        }
    }

    private fun prepareRatingItems(data: List<RatingItem>) =
            data.mapIndexed { index, (rank, _, exp, user) ->
                RatingItem(
                        if (rank == 0) index + 1 else rank,
                        ratingNamesGenerator.getName(user),
                        exp,
                        user
                )
            }

    fun changeRatingPeriod(pos: Int) {
        this.ratingPeriod = pos
        view?.onRatingAdapter(adapters[pos])
    }

    override fun attachView(view: AdaptiveRatingView) {
        super.attachView(view)

        view.onRatingAdapter(adapters[ratingPeriod])
        view.onLoading()

        if (error != null) {
            error?.let { onError(it) }
        } else {
            onLoadComplete()
        }
    }

    private fun onLoadComplete() {
        if (periodsLoaded == RATING_PERIODS.size) {
            view?.onComplete()
        }
    }

    private fun onError(throwable: Throwable) {
        error = throwable
        if (throwable is HttpException) {
            view?.onRequestError()
        } else {
            view?.onConnectivityError()
        }
    }

    fun retry() {
        error = null
        retrySubject.onNext(0)
    }

    fun destroy() {
        compositeDisposable.dispose()
    }
}
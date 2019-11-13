package org.stepic.droid.core.presenters

import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.adaptive.ui.adapters.AdaptiveRatingAdapter
import org.stepic.droid.adaptive.util.RatingNamesGenerator
import org.stepic.droid.core.presenters.contracts.AdaptiveRatingView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.addDisposable
import org.stepik.android.data.rating.source.RatingRemoteDataSource
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.model.adaptive.RatingItem
import retrofit2.HttpException
import javax.inject.Inject

class AdaptiveRatingPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,
    private val ratingRemoteDataSource: RatingRemoteDataSource,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val ratingNamesGenerator: RatingNamesGenerator,
    private val userRemoteDataSource: UserRemoteDataSource,

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
            compositeDisposable addDisposable resolveUsers(ratingRemoteDataSource.getRating(courseId, ITEMS_PER_PAGE, period))
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .doOnError(this::onError)
                    .retryWhen { x -> x.zipWith(retrySubject.toFlowable(BackpressureStrategy.BUFFER), left) }
                    .subscribeBy(this::onError) {
                        adapters[pos].set(it)
                        periodsLoaded++
                        onLoadComplete()
                    }
        }
    }

    private fun resolveUsers(single: Single<List<RatingItem>>): Single<List<RatingItem>> =
            single.flatMap {
                val userIds = it.filter{ it.isNotFake }.map { it.user }.toLongArray()
                if (userIds.isEmpty()) {
                    Single.just(emptyList())
                } else {
                    userRemoteDataSource.getUsers(*userIds)
                }.zipWith(Single.just(it))
            }.map { (users, items) ->
                items.mapIndexed { index, item ->
                    val user = users.find { it.id == item.user }
                    val name = user?.fullName ?: ratingNamesGenerator.getName(item.user)

                    item.copy(rank = if (item.rank == 0) index + 1 else item.rank, name = name)
                }
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
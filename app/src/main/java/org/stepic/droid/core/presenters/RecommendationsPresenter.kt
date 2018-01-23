package org.stepic.droid.core.presenters

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.adaptive.listeners.AdaptiveReactionListener
import org.stepic.droid.adaptive.listeners.AnswerListener
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.adaptive.model.Reaction
import org.stepic.droid.adaptive.model.RecommendationReaction
import org.stepic.droid.adaptive.ui.adapters.QuizCardsAdapter
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.contracts.RecommendationsView
import org.stepic.droid.di.adaptive.AdaptiveCourseScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.PersistentLastStep
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.web.Api
import org.stepic.droid.web.ViewAssignment
import org.stepic.droid.web.model.adaptive.RecommendationsResponse
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

@AdaptiveCourseScope
class RecommendationsPresenter
@Inject
constructor(
        private val api: Api,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val databaseFacade: DatabaseFacade,
        private val screenManager: ScreenManager
) : PresenterBase<RecommendationsView>(), AdaptiveReactionListener, AnswerListener {

    companion object {
        private const val CARDS_IN_CACHE = 6
        private const val MIN_CARDS_IN_CACHE = 4
    }

    private val compositeDisposable = CompositeDisposable()
    private val retrySubject = PublishSubject.create<Any>()

    private val cards = ArrayDeque<Card>()

    private val adapter = QuizCardsAdapter(this, this)

    private var cardDisposable: Disposable? = null

    private var error: Throwable? = null

    private var isCourseCompleted = false

    // todo: inject course id
    private var courseId = 0L

//    init {
//        createReaction(0, Reaction.INTERESTING)
//    }

    fun initCourse(courseId: Long) {
        this.courseId = courseId
        createReaction(0, Reaction.INTERESTING)
    }

    override fun attachView(view: RecommendationsView) {
        super.attachView(view)

        view.onLoading()

        when {
            isCourseCompleted -> view.onCourseCompleted()
            courseId == 0L -> view.onCourseNotSupported()
            else -> {
                resubscribe()
                error?.let(this::onError)
            }
        }

        view.onAdapter(adapter)
    }

    // methods for gamification
    override fun onCorrectAnswer(submissionId: Long) {}
    override fun onWrongAnswer() {}

    override fun createReaction(lessonId: Long, reaction: Reaction) {
        if (adapter.isEmptyOrContainsOnlySwipedCard(lessonId)) {
            view?.onLoading()
        }

        compositeDisposable.add(createReactionObservable(lessonId, reaction, cards.size + adapter.getItemCount())
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .doOnError(this::onError)
                .retryWhen { it.zipWith(retrySubject, BiFunction<Any, Any, Any> {a, _ -> a}) }
                .subscribe(this::onRecommendation, this::onError))
    }

    private fun onRecommendation(response: RecommendationsResponse) {
        val recommendations = response.recommendations
        if (recommendations == null || recommendations.isEmpty()) {
            isCourseCompleted = true
            view?.onCourseCompleted()
        } else {
            val size = cards.size
            recommendations
                    .filter { !isCardExists(it.lesson) }
                    .forEach { cards.add(Card(courseId, it.lesson)) }

            if (size == 0) resubscribe()
        }
    }

    private fun onError(throwable: Throwable?) {
        this.error = throwable
        when(throwable) {
            is HttpException -> view?.onRequestError()
            else -> view?.onConnectivityError()
        }
    }

    fun retry() {
        this.error = null
        retrySubject.onNext(0)
        view?.onLoading()

        if (cards.isNotEmpty()) {
            cards.peek().initCard()
            resubscribe()
        }
    }

    private fun resubscribe() {
        if (cards.isNotEmpty()) {
            if (cardDisposable != null && cardDisposable?.isDisposed == false) {
                cardDisposable?.dispose()
            }

            cardDisposable = cards.peek()
                    .subscribe(this::onCardDataLoaded, this::onError)
        }
    }

    private fun onCardDataLoaded(card: Card) {
        reportView(card)
        adapter.add(card)
        view?.onCardLoaded()
        cards.poll()
        resubscribe()
    }

    private fun isCardExists(lessonId: Long) =
            cards.any { it.lessonId == lessonId } || adapter.isCardExists(lessonId)

    override fun detachView(view: RecommendationsView) {
        adapter.detach()
        cardDisposable?.dispose()
        super.detachView(view)
    }

    fun destroy() {
        compositeDisposable.dispose()
        cards.forEach(Card::recycle)
        adapter.destroy()
    }

    private fun createReactionObservable(lesson: Long, reaction: Reaction, cacheSize: Int): Observable<RecommendationsResponse> {
        val responseObservable = api.getNextRecommendations(courseId, CARDS_IN_CACHE).toObservable()

        if (lesson != 0L) {
            val reactionCompletable = api
                    .createReaction(RecommendationReaction(lesson, reaction, sharedPreferenceHelper.profile?.id ?: 0))
            return if (cacheSize <= MIN_CARDS_IN_CACHE) {
                reactionCompletable.andThen(responseObservable)
            } else {
                reactionCompletable.toObservable()
            }
        }
        return responseObservable
    }

    private fun reportView(card: Card) {
        compositeDisposable.add(api.getUnits(courseId, card.lessonId)
                .subscribeOn(backgroundScheduler)
                .observeOn(backgroundScheduler)
                .subscribe({ response ->
                    val unit = response.units?.firstOrNull()
                    val stepId = card.step?.id ?: 0
                    unit?.assignments?.firstOrNull()?.let { assignmentId ->
                        screenManager.pushToViewedQueue(ViewAssignment(assignmentId, stepId))
                        databaseFacade.updateLastStep(PersistentLastStep(courseId, stepId, unit.id))
                    }
                }, {}))
    }
}
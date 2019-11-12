package org.stepic.droid.core.presenters

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.adaptive.listeners.AdaptiveReactionListener
import org.stepic.droid.adaptive.listeners.AnswerListener
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.adaptive.ui.adapters.QuizCardsAdapter
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.adaptive.util.ExpHelper
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.RecommendationsView
import org.stepic.droid.di.adaptive.AdaptiveCourseScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.getStepType
import org.stepic.droid.web.Api
import org.stepic.droid.web.model.adaptive.RecommendationsResponse
import org.stepik.android.data.recommendation.source.RecommendationRemoteDataSource
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.domain.view_assignment.interactor.ViewAssignmentReportInteractor
import org.stepik.android.model.adaptive.Reaction
import org.stepik.android.model.adaptive.RecommendationReaction
import retrofit2.HttpException
import java.util.ArrayDeque
import javax.inject.Inject

@AdaptiveCourseScope
class RecommendationsPresenter
@Inject
constructor(
    @CourseId
    private val courseId: Long,
    private val recommendationRemoteDataSource: RecommendationRemoteDataSource,
    private val unitRemoteDataSource: UnitRemoteDataSource,
    private val api: Api,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val databaseFacade: DatabaseFacade,
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val analytic: Analytic,
    private val viewAssignmentReportInteractor: ViewAssignmentReportInteractor
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

    private var exp: Long = 0
    private var streak: Long = 0

    init {
        createReaction(0, Reaction.INTERESTING)
        fetchLocalExp()
    }

    override fun attachView(view: RecommendationsView) {
        super.attachView(view)
        updateExp()

        view.onLoading()

        when {
            isCourseCompleted -> view.onCourseCompleted()
            !adaptiveCoursesResolver.isAdaptive(courseId) -> view.onCourseNotSupported()
            else -> {
                resubscribe()
                error?.let(this::onError)
            }
        }

        view.onAdapter(adapter)
    }

    private fun updateExp(showLevelDialog: Boolean = false) {
        val level = ExpHelper.getCurrentLevel(exp)

        val prev = ExpHelper.getNextLevelExp(level - 1)
        val next = ExpHelper.getNextLevelExp(level)

        view?.updateExp(exp, prev, next, level)

        if (showLevelDialog && level != ExpHelper.getCurrentLevel(exp - streak)) {
            view?.showNewLevelDialog(level)
        }
    }

    // methods for gamification
    override fun onCorrectAnswer(submissionId: Long) {
        changeExp(submissionId, isCorrect = true)
        view?.onStreak(streak)
        updateExp(showLevelDialog = true)

        if (!sharedPreferenceHelper.isAdaptiveExpTooltipWasShown) {
            view?.let{
                it.showExpTooltip()
                sharedPreferenceHelper.afterAdaptiveExpTooltipWasShown()
            }
        }
    }

    override fun onWrongAnswer(submissionId: Long) {
        if (streak > 1) {
            view?.onStreakLost()
        }
        changeExp(submissionId, isCorrect = false)
    }

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
        val responseObservable = recommendationRemoteDataSource.getNextRecommendations(courseId, CARDS_IN_CACHE).toObservable()

        if (lesson != 0L) {
            val reactionCompletable = recommendationRemoteDataSource
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
        val step = card.step ?: return

        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Steps.STEP_OPENED, mapOf(
                AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
                AmplitudeAnalytic.Steps.Params.NUMBER to step.position,
                AmplitudeAnalytic.Steps.Params.STEP to step.id
        ))

        compositeDisposable += unitRemoteDataSource
            .getUnitsByCourseAndLessonId(courseId, card.lessonId)
            .flatMapCompletable { units ->
                val unit = units.firstOrNull()

                val assignments = databaseFacade
                    .getAssignments(unit?.assignments ?: longArrayOf())
                    .firstOrNull()

                val course = databaseFacade
                    .getCourseById(courseId)

                viewAssignmentReportInteractor.reportViewAssignment(step, assignments, unit, course)
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(backgroundScheduler)
            .subscribeBy(emptyOnErrorStub)
    }

    private fun fetchLocalExp() {
        compositeDisposable.add(
            Single.fromCallable { databaseFacade.getExpForCourse(courseId) to databaseFacade.getStreakForCourse(courseId) }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    exp = it.first
                    streak = it.second
                    updateExp()
                    fetchExpFromAPI()
                }, emptyOnErrorStub)
        )
    }

    private fun fetchExpFromAPI() {
        compositeDisposable.add(
            api.restoreRating(courseId)
                    .subscribeOn(backgroundScheduler)
                    .map {
                        databaseFacade.syncExp(courseId, it.exp)
                    }
                    .observeOn(mainScheduler)
                    .subscribe({
                        exp = it
                        updateExp()
                    }, emptyOnErrorStub)
        )
    }

    private fun changeExp(submissionId: Long, isCorrect: Boolean) {
        if (isCorrect) {
            streak++
        } else { // reset streak on wrong answer
            if (streak == 0L) { // no need to store that info again
                return
            } else {
                streak = 0
            }
        }

        exp += streak
        compositeDisposable.add(
            Completable.fromCallable { databaseFacade.addLocalExpItem(streak, submissionId, courseId) }
                .andThen(syncRating())
                .subscribeOn(backgroundScheduler)
                .subscribeBy(emptyOnErrorStub)
        )
    }

    private fun syncRating() = Single.fromCallable { databaseFacade.getExpForCourse(courseId) }
            .flatMap { localExp -> api.putRating(courseId, localExp).toSingle { localExp } }
}
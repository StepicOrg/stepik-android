package org.stepic.droid.core.presenters

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.stepic.droid.adaptive.listeners.AdaptiveReactionListener
import org.stepic.droid.adaptive.listeners.AnswerListener
import org.stepic.droid.model.Submission
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.adaptive.model.Reaction
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.contracts.CardView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.getStepType
import org.stepic.droid.web.Api
import org.stepic.droid.web.SubmissionResponse
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CardPresenter(
        val card: Card,
        private val listener: AdaptiveReactionListener?,
        private val answerListener: AnswerListener?
) : PresenterBase<CardView>() {
    @Inject
    lateinit var api: Api

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    @Inject
    @field:BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    @Inject
    lateinit var analytic: Analytic

    private var submission: Submission? = null
    private var error: Throwable? = null

    private var disposable: Disposable? = null

    var isLoading = false
        private set

    init {
        App.componentManager()
                .adaptiveCourseComponent(card.courseId)
                .inject(this)
    }

    override fun attachView(view: CardView) {
        super.attachView(view)
        view.setStep(card.step)
        view.setTitle(card.lesson?.title)
        view.setQuestion(card.step?.block?.text)
        view.getQuizViewDelegate().setAttempt(card.attempt)

        if (isLoading) view.onSubmissionLoading()
        submission?.let { view.setSubmission(it, false) }
        error?.let { onError(it) }
    }

    fun detachView() {
        view?.let {
            if (submission == null || submission?.status == Submission.Status.LOCAL) {
                submission = Submission(it.getQuizViewDelegate().createReply(), 0, Submission.Status.LOCAL) // cache current choices state
            }
            super.detachView(it)
        }
    }

    fun destroy() {
        App.componentManager()
                .releaseAdaptiveCourseComponent(card.courseId)
        card.recycle()
        disposable?.dispose()
    }

    fun createReaction(reaction: Reaction) {
        val lesson = card.lessonId
        when(reaction) {
            Reaction.NEVER_AGAIN -> {
                if (card.correct) {
                    analytic.reportEventValue(Analytic.Adaptive.REACTION_EASY_AFTER_CORRECT, lesson)
                }
                analytic.reportEventValue(Analytic.Adaptive.REACTION_EASY, lesson)
            }

            Reaction.MAYBE_LATER -> {
                if (card.correct) {
                    analytic.reportEventValue(Analytic.Adaptive.REACTION_HARD_AFTER_CORRECT, lesson)
                }
                analytic.reportEventValue(Analytic.Adaptive.REACTION_HARD, lesson)
            }
        }
        listener?.createReaction(lesson, reaction)
    }

    fun createSubmission() {
        if (disposable == null || disposable?.isDisposed != false) {
            view?.onSubmissionLoading()
            isLoading = true
            error = null

            val submission = Submission(view?.getQuizViewDelegate()?.createReply(), card.attempt?.id ?: 0)
            disposable = api.createNewSubmissionReactive(submission)
                    .andThen(api.getSubmissionsReactive(submission.attempt))
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe(this::onSubmissionLoaded, this::onError)

            val bundle = Bundle()
            bundle.putString(Analytic.Steps.STEP_TYPE_KEY, card.step.getStepType())
            analytic.reportEvent(Analytic.Steps.SUBMISSION_CREATED, bundle)
            analytic.reportEvent(Analytic.Adaptive.ADAPTIVE_SUBMISSION_CREATED)
        }
    }

    fun retrySubmission() {
        submission = null
    }

    private fun onSubmissionLoaded(submissionResponse: SubmissionResponse) {
        submission = submissionResponse.submissions.firstOrNull()
        submission?.let {
            if (it.status == Submission.Status.EVALUATION) {
                disposable =  api.getSubmissionsReactive(it.attempt)
                        .delay(1, TimeUnit.SECONDS)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(this::onSubmissionLoaded, this::onError)
            } else {
                isLoading = false

                if (it.status == Submission.Status.CORRECT) {
                    analytic.reportEvent(Analytic.Steps.CORRECT_SUBMISSION_FILL, (card.step?.id ?: 0).toString())
                    listener?.createReaction(card.lessonId, Reaction.SOLVED)
                    answerListener?.onCorrectAnswer(it.id)
                    card.onCorrect()
                }
                if (it.status == Submission.Status.WRONG) {
                    analytic.reportEvent(Analytic.Steps.WRONG_SUBMISSION_FILL, (card.step?.id ?: 0).toString())
                    answerListener?.onWrongAnswer(it.id)
                }

                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Steps.SUBMISSION_MADE, mapOf(
                        AmplitudeAnalytic.Steps.Params.TYPE to card.step.getStepType(),
                        AmplitudeAnalytic.Steps.Params.STEP to (card.step?.id?.toString() ?: "0")
                ))

                view?.setSubmission(it, true)
            }
        }
    }

    private fun onError(error: Throwable) {
        isLoading = false
        this.error = error
        if (error is HttpException) {
            view?.onSubmissionRequestError()
        } else {
            view?.onSubmissionConnectivityError()
        }
    }
}
package org.stepic.droid.adaptive.model

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.stepic.droid.base.App
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Lesson
import org.stepik.android.model.Step
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class Card(
        val courseId: Long,
        val lessonId: Long,

        var lesson: Lesson? = null,
        var step: Step? = null,
        var attempt: Attempt? = null
) : Single<Card>() {
    @Inject
    lateinit var lessonRepository: LessonRepository
    @Inject
    lateinit var stepRepository: StepRepository

    @Inject
    lateinit var stepQuizInteractor: StepQuizInteractor

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    @Inject
    @field:BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    init {
        App.componentManager()
                .adaptiveCourseComponent(courseId)
                .inject(this)
    }

    private var observer: SingleObserver<in Card>? = null

    private var error: Throwable? = null

    private var lessonDisposable: Disposable? = null
    private var stepSubscription: Disposable? = null
    private var attemptDisposable: Disposable? = null

    private val compositeDisposable = CompositeDisposable()

    var correct = false
        private set

    fun initCard() {
        error = null

        if (stepSubscription == null || stepSubscription?.isDisposed == true && step == null) {
            stepSubscription = stepRepository.getStepByLessonId(lessonId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe({ setStep(it) }, { onError(it) })
        } else {
            setStep(step)
        }

        if (lessonDisposable == null || lessonDisposable?.isDisposed == true && lesson == null) {
            lessonDisposable = lessonRepository.getLesson(lessonId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe({ setLesson(it) }, { onError(it) })
        }
    }

    private fun setStep(newStep: Step?) = newStep?.let {
        this.step = newStep
        if (attemptDisposable == null || attemptDisposable?.isDisposed == true && attempt == null) {
            attemptDisposable = stepQuizInteractor.createAttempt(newStep.id)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe({ setAttempt(it) }, { onError(it) })
        }

        notifyDataChanged()
    }

    private fun setLesson(lesson: Lesson?) = lesson?.let {
        this.lesson = it
        notifyDataChanged()
    }

    private fun setAttempt(attempt: Attempt?) = attempt?.let {
        this.attempt = it
        notifyDataChanged()
    }

    private fun onError(error: Throwable?) {
        this.error = error
        notifyDataChanged()
    }

    private fun notifyDataChanged() = observer?.let {
        error?.let(it::onError)

        if (lesson != null && step != null && attempt != null) {
            it.onSuccess(this)
        }
    }

    /**
     * Free resources
     */
    fun recycle() {
        App.componentManager()
                .releaseAdaptiveCourseComponent(courseId)
        lessonDisposable?.dispose()
        stepSubscription?.dispose()
        attemptDisposable?.dispose()
        compositeDisposable.dispose()
        observer = null
    }

    override fun subscribeActual(observer: SingleObserver<in Card>) {
        this.observer = observer
        initCard()
        notifyDataChanged()
    }

    fun onCorrect() {
        correct = true
    }
}
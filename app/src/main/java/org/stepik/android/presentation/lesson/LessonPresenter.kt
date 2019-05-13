package org.stepik.android.presentation.lesson

import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.lesson.interactor.LessonInteractor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.domain.lesson.model.LessonDeepLinkData
import javax.inject.Inject

class LessonPresenter
@Inject
constructor(
    private val lessonInteractor: LessonInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<LessonView>() {
    private var state: LessonView.State = LessonView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: LessonView) {
        super.attachView(view)
        view.setState(state)
    }

    /**
     * Data initialization variants
     */
    fun onLesson(lesson: Lesson, unit: Unit, section: Section, forceUpdate: Boolean = false) {
        obtainLessonData(lessonInteractor.getLessonData(lesson, unit, section), forceUpdate)
    }

    fun onLastStep(lastStep: LastStep, forceUpdate: Boolean = false) {
        obtainLessonData(lessonInteractor.getLessonData(lastStep), forceUpdate)
    }

    fun onDeepLink(deepLinkData: LessonDeepLinkData, forceUpdate: Boolean = false) {
        obtainLessonData(lessonInteractor.getLessonData(deepLinkData), forceUpdate)
    }

    fun onEmptyData() {
        if (state == LessonView.State.Idle) {
            state = LessonView.State.LessonNotFound
        }
    }

    private fun obtainLessonData(lessonDataSource: Maybe<LessonData>, forceUpdate: Boolean = false) {
        if (state != LessonView.State.Idle &&
            !(state == LessonView.State.NetworkError && forceUpdate)
        ) {
            return
        }

        state = LessonView.State.Loading
        compositeDisposable += lessonDataSource
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = LessonView.State.LessonNotFound },
                onSuccess  = { state = LessonView.State.LessonLoaded(it, LessonView.StepsState.Idle) /* resolve steps state */ },
                onError    = { state = LessonView.State.NetworkError }
            )
    }

    fun onShowLessonInfoClicked(position: Int) {
        val state = (state as? LessonView.State.LessonLoaded)
            ?: return

        val stepWorth = (state.stepsState as? LessonView.StepsState.Loaded)
            ?.steps
            ?.getOrNull(position)
            ?.step
            ?.worth
            ?: return

        view?.showLessonInfoTooltip(stepWorth, state.lessonData.lesson.timeToComplete, 99)
    }
}
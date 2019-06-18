package org.stepik.android.presentation.step

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.interactor.StepNavigationInteractor
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class StepPresenter
@Inject
constructor(
    private val stepNavigationInteractor: StepNavigationInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepView>() {
    private var state: StepView.State = StepView.State.Idle

    private var stepNavigationDirections: Set<StepNavigationDirection> = emptySet()
        set(value) {
            field = value
            view?.setNavigation(value)
        }

    private var isBlockingLoading: Boolean = false
        set(value) {
            field = value
            view?.setBlockingLoading(value)
        }

    override fun attachView(view: StepView) {
        super.attachView(view)
        view.setBlockingLoading(isBlockingLoading)
        view.setNavigation(stepNavigationDirections)
    }

    fun onLessonData(stepWrapper: StepPersistentWrapper, lessonData: LessonData) {
        if (state != StepView.State.Idle) return

        state = StepView.State.Loaded(stepWrapper, lessonData)
        fetchNavigation(stepWrapper, lessonData)
    }

    /**
     * Navigation
     */
    private fun fetchNavigation(stepWrapper: StepPersistentWrapper, lessonData: LessonData) {
        compositeDisposable += stepNavigationInteractor
            .getStepNavigationDirections(stepWrapper.step, lessonData)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { stepNavigationDirections = it },
                onError = emptyOnErrorStub
            )
    }

    fun onStepDirectionClicked(stepNavigationDirection: StepNavigationDirection) {
        val state = (state as? StepView.State.Loaded)
            ?: return

        compositeDisposable += stepNavigationInteractor
            .getLessonDataForDirection(stepNavigationDirection, state.stepWrapper.step, state.lessonData)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSubscribe { isBlockingLoading = true }
            .doFinally { isBlockingLoading = false }
            .subscribeBy(
                onSuccess = { view?.showLesson(stepNavigationDirection, lessonData = it) },
                onError = emptyOnErrorStub
            )
    }
}
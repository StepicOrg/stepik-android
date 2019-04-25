package org.stepik.android.presentation.lesson

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class LessonPresenter
@Inject
constructor(
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

    fun onShowLessonInfoClicked(position: Int) {
        val state = (state as? LessonView.State.LessonLoaded)
            ?: return

        val stepWorth = (state.stepsState as? LessonView.StepsState.Loaded)
            ?.steps
            ?.getOrNull(position)
            ?.step
            ?.worth
            ?: return

        view?.showLessonInfoTooltip(stepWorth, state.lesson.timeToComplete, 99)
    }
}
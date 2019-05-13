package org.stepik.android.presentation.lesson

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.Lesson

interface LessonView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        object LessonNotFound : State()
        object EmptyLogin : State()
        object NetworkError : State()

        data class EmptyLesson(
            val lessonData: LessonData
        ) : State()

        data class LessonLoaded(
            val lessonData: LessonData,
            val stepsState: StepsState
        ) : State()
    }

    sealed class StepsState {
        object Idle : StepsState()
        object Loading : StepsState()
        object NetworkError : StepsState()
        class Loaded(
            val steps: List<StepPersistentWrapper>
        ) : StepsState()
    }

    fun setState(state: State)

    fun showLessonInfoTooltip(stepWorth: Long, lessonTimeToComplete: Long, certificateThreshold: Long)
}
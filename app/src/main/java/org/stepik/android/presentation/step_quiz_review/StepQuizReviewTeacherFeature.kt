package org.stepik.android.presentation.step_quiz_review

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.presentation.step_quiz.StepQuizFeature

interface StepQuizReviewTeacherFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()

        data class Data(
            val instructionType: ReviewStrategyType,
            val availableReviewCount: Int,
            val quizState: StepQuizFeature.State.AttemptLoaded
        ) : State()
    }

    sealed class Message {
        data class InitWithStep(
            val stepWrapper: StepPersistentWrapper,
            val lessonData: LessonData,
            val instructionType: ReviewStrategyType,
            val forceUpdate: Boolean = false
        ) : Message()
        data class FetchDataSuccess(
            val instructionType: ReviewStrategyType,
            val reviewSession: ReviewSession?,
            val quizState: StepQuizFeature.State
        ) : Message()
        object FetchDataError : Message()

        /**
         * Step Quiz Message Wrapper
         */
        data class StepQuizMessage(val message: StepQuizFeature.Message) : Message()
    }

    sealed class Action {
        data class FetchData(
            val stepWrapper: StepPersistentWrapper,
            val lessonData: LessonData,
            val instructionType: ReviewStrategyType
        ) : Action()

        /**
         * Step Quiz Action Wrapper
         */
        data class StepQuizAction(val action: StepQuizFeature.Action) : Action()

        sealed class ViewAction : Action()
    }
}
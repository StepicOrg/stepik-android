package org.stepik.android.view.injection.step

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.injection.attempt.AttemptDataModule
import org.stepik.android.view.injection.discussion_thread.DiscussionThreadDataModule
import org.stepik.android.view.injection.step_content.StepContentModule
import org.stepik.android.view.injection.step_content_text.TextStepContentComponent
import org.stepik.android.view.injection.step_content_video.VideoStepContentComponent
import org.stepik.android.view.injection.step_source.StepSourceModule
import org.stepik.android.view.injection.step_quiz.StepQuizModule
import org.stepik.android.view.injection.step_quiz.StepQuizPresentationModule
import org.stepik.android.view.injection.step_source.StepSourceDataModule
import org.stepik.android.view.injection.submission.SubmissionDataModule
import org.stepik.android.view.step.ui.fragment.StepFragment
import org.stepik.android.view.step_source.ui.dialog.EditStepSourceDialogFragment
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_unsupported.ui.fragment.UnsupportedStepQuizFragment

@Subcomponent(modules = [
    StepModule::class,
    StepSourceModule::class,
    StepContentModule::class,
    StepQuizModule::class,

    StepQuizPresentationModule::class,
    AttemptDataModule::class,
    DiscussionThreadDataModule::class,
    SubmissionDataModule::class,
    StepSourceDataModule::class,
    StepWrapperBusModule::class
])
@StepScope
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent

        @BindsInstance
        fun lessonData(lessonData: LessonData): Builder

        @BindsInstance
        fun stepWrapper(stepPersistentWrapper: StepPersistentWrapper): Builder
    }

    fun videoStepContentComponentBuilder(): VideoStepContentComponent.Builder
    fun textStepContentComponentBuilder(): TextStepContentComponent.Builder

    fun inject(stepFragment: StepFragment)
    fun inject(editStepContentDialogFragment: EditStepSourceDialogFragment)
    fun inject(defaultStepQuizFragment: DefaultStepQuizFragment)
    fun inject(unsupportedStepQuizFragment: UnsupportedStepQuizFragment)
}
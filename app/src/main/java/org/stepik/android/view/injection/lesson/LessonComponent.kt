package org.stepik.android.view.injection.lesson

import dagger.Subcomponent
import org.stepik.android.view.injection.assignment.AssignmentDataModule
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.discussion_thread.DiscussionThreadDataModule
import org.stepik.android.view.injection.feedback.FeedbackDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule
import org.stepik.android.view.injection.review_session.ReviewSessionDataModule
import org.stepik.android.view.injection.section.SectionDataModule
import org.stepik.android.view.injection.step.StepDataModule
import org.stepik.android.view.injection.unit.UnitDataModule
import org.stepik.android.view.injection.user_activity.UserActivityDataModule
import org.stepik.android.view.injection.view_assignment.ViewAssignmentDataModule
import org.stepik.android.view.lesson.ui.activity.LessonActivity

@Subcomponent(modules = [
    LessonModule::class,
    LessonDataModule::class,

    UnitDataModule::class,
    SectionDataModule::class,
    CourseDataModule::class,
    LastStepDataModule::class,

    AssignmentDataModule::class,
    StepDataModule::class,
    ProgressDataModule::class,
    ViewAssignmentDataModule::class,

    FeedbackDataModule::class,
    UserActivityDataModule::class,
    DiscussionThreadDataModule::class,

    ReviewSessionDataModule::class
])
interface LessonComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LessonComponent
    }

    fun inject(lessonActivity: LessonActivity)
}
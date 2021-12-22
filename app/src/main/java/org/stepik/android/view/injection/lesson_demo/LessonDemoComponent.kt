package org.stepik.android.view.injection.lesson_demo

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepik.android.model.Course
import org.stepik.android.view.lesson_demo.ui.dialog.LessonDemoCompleteBottomSheetDialogFragment

@Subcomponent(modules = [
    LessonDemoPresentationModule::class
])
interface LessonDemoComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LessonDemoComponent

        @BindsInstance
        fun course(course: Course): Builder
    }
    fun inject(lessonDemoCompleteBottomSheetDialogFragment: LessonDemoCompleteBottomSheetDialogFragment)
}
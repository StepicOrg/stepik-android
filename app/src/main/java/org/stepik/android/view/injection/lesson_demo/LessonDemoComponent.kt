package org.stepik.android.view.injection.lesson_demo

import dagger.Subcomponent
import org.stepik.android.view.lesson_demo.ui.dialog.LessonDemoCompleteBottomSheetDialogFragment

@Subcomponent(modules = [
    LessonDemoPresentationModule::class
])
interface LessonDemoComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LessonDemoComponent
    }
    fun inject(lessonDemoCompleteBottomSheetDialogFragment: LessonDemoCompleteBottomSheetDialogFragment)
}
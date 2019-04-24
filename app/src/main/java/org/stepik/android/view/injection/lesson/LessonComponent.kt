package org.stepik.android.view.injection.lesson

import dagger.Subcomponent
import org.stepik.android.view.lesson.ui.activity.LessonActivity

@Subcomponent(modules = [
    LessonModule::class,
    LessonDataModule::class
])
interface LessonComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LessonComponent
    }

    fun inject(lessonActivity: LessonActivity)
}
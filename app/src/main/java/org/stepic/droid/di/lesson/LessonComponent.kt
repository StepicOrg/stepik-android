package org.stepic.droid.di.lesson

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.LessonFragment

@LessonScope
@Subcomponent(modules = arrayOf(LessonModule::class))
interface LessonComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LessonComponent
    }

    fun inject(lessonFragment: LessonFragment)
}

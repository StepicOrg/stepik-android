package org.stepic.droid.di.lesson

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.LessonFragment
import org.stepik.android.view.injection.unit.UnitDataModule

@LessonScope
@Subcomponent(modules = [
    LessonModule::class,
    UnitDataModule::class
])
interface LessonComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LessonComponent
    }

    fun inject(lessonFragment: LessonFragment)
}

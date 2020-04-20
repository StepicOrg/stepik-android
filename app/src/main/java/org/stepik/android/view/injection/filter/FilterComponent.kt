package org.stepik.android.view.injection.filter

import dagger.Subcomponent
import org.stepik.android.view.filter.ui.dialog.CoursesLangDialogFragment

@Subcomponent(modules = [FilterModule::class])
interface FilterComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): FilterComponent
    }

    fun inject(coursesLangDialogFragment: CoursesLangDialogFragment)
}
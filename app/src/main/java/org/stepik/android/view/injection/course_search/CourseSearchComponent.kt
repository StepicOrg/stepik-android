package org.stepik.android.view.injection.course_search

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.course_search.dialog.CourseSearchDialogFragment
import org.stepik.android.view.injection.search.CourseSearchSuggestionsPresentationModule

@Subcomponent(modules = [
    CourseSearchSuggestionsPresentationModule::class
])
interface CourseSearchComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseSearchComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun inject(courseSearchDialogFragment: CourseSearchDialogFragment)
}
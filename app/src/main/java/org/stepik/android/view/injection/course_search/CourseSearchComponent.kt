package org.stepik.android.view.injection.course_search

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.course_search.dialog.CourseSearchDialogFragment
import org.stepik.android.view.injection.discussion_thread.DiscussionThreadDataModule
import org.stepik.android.view.injection.search.CourseSearchSuggestionsPresentationModule
import org.stepik.android.view.injection.search.SearchDataModule
import org.stepik.android.view.injection.section.SectionDataModule
import org.stepik.android.view.injection.unit.UnitDataModule
import org.stepik.android.view.injection.user.UserDataModule

@Subcomponent(modules = [
    CourseSearchPresentationModule::class,
    CourseSearchSuggestionsPresentationModule::class,
    SearchDataModule::class,
    UserDataModule::class,
    SectionDataModule::class,
    UnitDataModule::class,
    DiscussionThreadDataModule::class
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
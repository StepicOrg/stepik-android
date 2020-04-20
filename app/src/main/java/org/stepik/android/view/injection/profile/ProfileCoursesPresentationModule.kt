package org.stepik.android.view.injection.profile

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.profile_courses.ProfileCoursesPresenter
import org.stepik.android.presentation.profile_courses.ProfileCoursesView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer

@Module
abstract class ProfileCoursesPresentationModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfileCoursesPresenter::class)
    internal abstract fun bindProfileCoursesPresenter(ProfileCoursesPresenter: ProfileCoursesPresenter): ViewModel

    @Binds
    internal abstract fun bindCourseContinueViewContainer(@ProfileCoursesScope viewContainer: PresenterViewContainer<ProfileCoursesView>): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @ProfileCoursesScope
        fun provideViewContainer(): PresenterViewContainer<ProfileCoursesView> =
            DefaultPresenterViewContainer()
    }
}
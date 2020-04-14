package org.stepic.droid.di.course_list

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepik.android.presentation.course_continue.CourseContinueView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer
import java.util.concurrent.Executors

@Module
abstract class CourseListModule {

    @Binds
    internal abstract fun bindCourseContinueViewContainer(
        @CourseListScope viewContainer: PresenterViewContainer<CourseContinueView>
    ): ViewContainer<out CourseContinueView>

    @Module
    companion object {
        @JvmStatic
        @CourseListScope
        @Provides
        fun provideSingleThreadExecutor(): SingleThreadExecutor =
                SingleThreadExecutor(Executors.newSingleThreadExecutor())

        @Provides
        @JvmStatic
        @CourseListScope
        fun provideViewContainer(): PresenterViewContainer<CourseContinueView> =
            DefaultPresenterViewContainer()
    }
}

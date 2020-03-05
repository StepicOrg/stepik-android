package org.stepic.droid.di.course_list

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.util.resolvers.SearchResolverImpl
import org.stepik.android.presentation.course_continue.CourseContinueView
import ru.nobird.android.presentation.base.DefaultPresenterViewContainer
import ru.nobird.android.presentation.base.PresenterViewContainer
import ru.nobird.android.presentation.base.ViewContainer
import java.util.concurrent.Executors

@Module
abstract class CourseListModule {

    @CourseListScope
    @Binds
    abstract fun provideSearchResolver(searchResolver: SearchResolverImpl): SearchResolver

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

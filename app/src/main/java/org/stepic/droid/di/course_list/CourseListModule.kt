package org.stepic.droid.di.course_list

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.util.resolvers.SearchResolverImpl
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
    internal abstract fun bindCourseShowableContainer(
        @CourseListScope viewContainer: PresenterViewContainer<ContinueCourseView>
    ): ViewContainer<out org.stepik.android.presentation.course_continue.ContinueCourseView>

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
        fun provideViewContainer(): PresenterViewContainer<ContinueCourseView> =
            DefaultPresenterViewContainer()
    }
}

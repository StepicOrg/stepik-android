package org.stepic.droid.di.course_list

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.util.resolvers.SearchResolverImpl
import java.util.concurrent.Executors

@Module
abstract class CourseListModule {

    @CourseListScope
    @Binds
    abstract fun provideSearchResolver(searchResolver: SearchResolverImpl): SearchResolver


    @Module
    companion object {
        @JvmStatic
        @CourseListScope
        @Provides
        fun provideSingleThreadExecutor(): SingleThreadExecutor =
                SingleThreadExecutor(Executors.newSingleThreadExecutor())
    }
}

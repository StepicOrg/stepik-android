package org.stepic.droid.di.course_list

import dagger.Binds
import dagger.Module
import org.stepic.droid.util.resolvers.SearchResolver
import org.stepic.droid.util.resolvers.SearchResolverImpl

@Module
interface CourseListModule {

    @CourseListScope
    @Binds
    fun provideSearchResolver(searchResolver: SearchResolverImpl): SearchResolver
}

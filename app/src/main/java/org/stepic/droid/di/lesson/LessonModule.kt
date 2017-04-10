package org.stepic.droid.di.lesson

import dagger.Binds
import dagger.Module
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepic.droid.util.resolvers.StepTypeResolverImpl

@Module
interface LessonModule {
    @Binds
    @LessonScope
    fun bindStepTypeResolver(stepTypeResolver: StepTypeResolverImpl): StepTypeResolver
}

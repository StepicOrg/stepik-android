package org.stepic.droid.persistence.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.content.StepContentResolverImpl
import org.stepic.droid.persistence.content.processors.StepContentProcessor
import org.stepic.droid.persistence.content.processors.VideoStepContentProcessor

@Module
interface ContentModule {

    @Binds
    @PersistenceScope
    @IntoSet
    fun bindVideoStepContentProcessor(videoStepContentProcessor: VideoStepContentProcessor): StepContentProcessor

    @Binds
    @PersistenceScope
    fun bindStepContentResolver(stepContentResolverImpl: StepContentResolverImpl): StepContentResolver

}
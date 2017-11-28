package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.GoogleApiChecker
import org.stepic.droid.core.GoogleApiCheckerImpl

@Module
interface GoogleModule {
    @Binds
    fun bindGoogleChecker(googleApiCheckerImpl: GoogleApiCheckerImpl): GoogleApiChecker
}

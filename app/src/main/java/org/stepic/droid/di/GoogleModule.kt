package org.stepic.droid.di

import dagger.Module
import org.stepic.droid.core.GoogleApiChecker
import org.stepic.droid.core.GoogleApiCheckerImpl

@Module
interface GoogleModule {
    fun bindGoogleChecker(googleApiCheckerImpl: GoogleApiCheckerImpl): GoogleApiChecker
}

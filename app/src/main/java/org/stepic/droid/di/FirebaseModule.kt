package org.stepic.droid.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides

@Module
class FirebaseModule {

    @Provides
    @AppSingleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
}

package org.stepic.droid.di

import android.content.Context
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides

@Module
class FirebaseModule {

    @Provides
    @AppSingleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @AppSingleton
    fun provideFirebaseAppIndex(context: Context): FirebaseAppIndex = FirebaseAppIndex.getInstance(context)

    @Provides
    @AppSingleton
    fun provideFirebaseUserActions(context: Context): FirebaseUserActions = FirebaseUserActions.getInstance(context)
}

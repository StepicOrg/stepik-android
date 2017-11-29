package org.stepic.droid.di

import com.google.firebase.iid.FirebaseInstanceId
import dagger.Module
import dagger.Provides

@Module
class FirebaseModule {

    @Provides
    @AppSingleton
    fun provideFirebaseInstanceId(): FirebaseInstanceId = FirebaseInstanceId.getInstance()

}

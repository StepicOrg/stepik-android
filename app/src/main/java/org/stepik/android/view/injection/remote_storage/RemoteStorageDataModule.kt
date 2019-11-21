package org.stepik.android.view.injection.remote_storage

import dagger.Module
import dagger.Provides
import org.stepik.android.remote.remote_storage.service.RemoteStorageService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class RemoteStorageDataModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideRemoteStorageService(@Authorized retrofit: Retrofit): RemoteStorageService =
            retrofit.create(RemoteStorageService::class.java)
    }
}
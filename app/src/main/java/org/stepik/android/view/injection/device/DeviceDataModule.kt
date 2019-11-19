package org.stepik.android.view.injection.device

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.device.repository.DeviceRepositoryImpl
import org.stepik.android.data.device.source.DeviceRemoteDataSource
import org.stepik.android.domain.device.repository.DeviceRepository
import org.stepik.android.remote.device.DeviceRemoteDataSourceImpl
import org.stepik.android.remote.device.service.DeviceService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class DeviceDataModule {
    @Binds
    internal abstract fun bindDeviceRepository(
        deviceRepositoryImpl: DeviceRepositoryImpl
    ): DeviceRepository

    @Binds
    internal abstract fun bindDeviceRemoteDataSource(
        deviceRemoteDataSourceImpl: DeviceRemoteDataSourceImpl
    ): DeviceRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideDeviceService(@Authorized retrofit: Retrofit): DeviceService =
            retrofit.create(DeviceService::class.java)
    }
}
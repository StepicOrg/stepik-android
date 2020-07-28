package org.stepik.android.view.injection.analytic

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.AnalyticStubImpl
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.concurrency.MainHandlerAnalyticImpl
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.cache.analytic.AnalyticCacheDataSourceImpl
import org.stepik.android.data.analytic.repository.AnalyticRepositoryImpl
import org.stepik.android.data.analytic.source.AnalyticCacheDataSource
import org.stepik.android.data.analytic.source.AnalyticRemoteDataSource
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import org.stepik.android.remote.analytic.AnalyticRemoteDataSourceImpl
import org.stepik.android.remote.analytic.service.AnalyticService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class AnalyticModule {
    @Binds
    internal abstract fun bindAnalyticRepository(
        analyticRepositoryImpl: AnalyticRepositoryImpl
    ): AnalyticRepository

    @Binds
    internal abstract fun bindAnalyticRemoteDataSource(
        analyticRemoteDataSourceImpl: AnalyticRemoteDataSourceImpl
    ): AnalyticRemoteDataSource

    @Binds
    internal abstract fun bindAnalyticCacheDataSource(
        analyticCacheDataSourceImpl: AnalyticCacheDataSourceImpl
    ): AnalyticCacheDataSource

    @Binds
    internal abstract fun bindAnalyticStub(
        analyticStubImpl: AnalyticStubImpl
    ): Analytic

    @Binds
    internal abstract fun provideHandlerForUIThread(
        mainHandler: MainHandlerAnalyticImpl
    ): MainHandler

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAnalyticService(@Authorized retroFit: Retrofit): AnalyticService =
            retroFit.create(AnalyticService::class.java)

        @Provides
        @JvmStatic
        @BackgroundScheduler
        internal fun provideBackgroundScheduler(): Scheduler =
            Schedulers.io()
    }
}
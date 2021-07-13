package org.stepik.android.view.injection.course_revenue

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.course_revenue.repository.CourseBenefitsRepositoryImpl
import org.stepik.android.data.course_revenue.source.CourseBenefitsRemoteDataSource
import org.stepik.android.domain.course_revenue.repository.CourseBenefitsRepository
import org.stepik.android.remote.course_revenue.CourseBenefitsRemoteDataSourceImpl
import org.stepik.android.remote.course_revenue.service.CourseBenefitsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CourseBenefitsDataModule {
    @Binds
    internal abstract fun bindCourseBenefitsRepository(
        courseBenefitsRepositoryImpl: CourseBenefitsRepositoryImpl
    ): CourseBenefitsRepository

    @Binds
    internal abstract fun bindCourseBenefitsRemoteDataSource(
        courseBenefitsRemoteDataSourceImpl: CourseBenefitsRemoteDataSourceImpl
    ): CourseBenefitsRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseBenefitsService(@Authorized retrofit: Retrofit): CourseBenefitsService =
            retrofit.create()
    }
}
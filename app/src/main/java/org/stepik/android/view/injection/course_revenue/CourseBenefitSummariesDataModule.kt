package org.stepik.android.view.injection.course_revenue

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.course_revenue.repository.CourseBenefitSummariesRepositoryImpl
import org.stepik.android.data.course_revenue.source.CourseBenefitSummariesRemoteDataSource
import org.stepik.android.domain.course_revenue.repository.CourseBenefitSummariesRepository
import org.stepik.android.remote.course_revenue.CourseBenefitSummariesRemoteDataSourceImpl
import org.stepik.android.remote.course_revenue.service.CourseBenefitSummariesService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CourseBenefitSummariesDataModule {
    @Binds
    internal abstract fun bindCourseBenefitSummariesRepository(
        courseBenefitSummariesRepositoryImpl: CourseBenefitSummariesRepositoryImpl
    ): CourseBenefitSummariesRepository

    @Binds
    internal abstract fun bindCourseBenefitSummariesRemoteDataSource(
        courseBenefitSummariesRemoteDataSourceImpl: CourseBenefitSummariesRemoteDataSourceImpl
    ): CourseBenefitSummariesRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseBenefitSummariesService(@Authorized retrofit: Retrofit): CourseBenefitSummariesService =
            retrofit.create()
    }
}
package org.stepik.android.view.injection.course_benefits

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.course_benefits.repository.CourseBenefitSummariesRepositoryImpl
import org.stepik.android.data.course_benefits.source.CourseBenefitSummariesRemoteDataSource
import org.stepik.android.domain.course_benefits.repository.CourseBenefitSummariesRepository
import org.stepik.android.remote.course_benefits.CourseBenefitSummariesRemoteDataSourceImpl
import org.stepik.android.remote.course_benefits.service.CourseBenefitSummariesService
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
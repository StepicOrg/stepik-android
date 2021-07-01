package org.stepik.android.view.injection.course_benefits

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.course_benefits.repository.CourseBenefitByMonthsRepositoryImpl
import org.stepik.android.data.course_benefits.source.CourseBenefitByMonthsRemoteDataSource
import org.stepik.android.domain.course_benefits.repository.CourseBenefitByMonthsRepository
import org.stepik.android.remote.course_benefits.CourseBenefitByMonthsRemoteDataSourceImpl
import org.stepik.android.remote.course_benefits.service.CourseBenefitByMonthsService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CourseBenefitByMonthsDataModule {
    @Binds
    internal abstract fun bindCourseBenefitByMonthRepository(
        courseBenefitByMonthsRepositoryImpl: CourseBenefitByMonthsRepositoryImpl
    ): CourseBenefitByMonthsRepository

    @Binds
    internal abstract fun bindCourseBenefitByMonthsRemoteDataSource(
        courseBenefitByMonthsRemoteDataSourceImpl: CourseBenefitByMonthsRemoteDataSourceImpl
    ): CourseBenefitByMonthsRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseBenefitByMonthsService(@Authorized retrofit: Retrofit): CourseBenefitByMonthsService =
            retrofit.create()
    }
}
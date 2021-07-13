package org.stepik.android.view.injection.course_revenue

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.course_revenue.repository.CourseBeneficiariesRepositoryImpl
import org.stepik.android.data.course_revenue.source.CourseBeneficiariesRemoteDataSource
import org.stepik.android.domain.course_revenue.repository.CourseBeneficiariesRepository
import org.stepik.android.remote.course_revenue.CourseBeneficiariesRemoteDataSourceImpl
import org.stepik.android.remote.course_revenue.service.CourseBeneficiariesService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CourseBeneficiariesDataModule {
    @Binds
    internal abstract fun bindCourseBeneficiariesRepository(
        courseBeneficiariesRepositoryImpl: CourseBeneficiariesRepositoryImpl
    ): CourseBeneficiariesRepository

    @Binds
    internal abstract fun bindCourseBeneficiariesRemoteDataSource(
        courseBeneficiariesRemoteDataSourceImpl: CourseBeneficiariesRemoteDataSourceImpl
    ): CourseBeneficiariesRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseBeneficiariesService(@Authorized retrofit: Retrofit): CourseBeneficiariesService =
            retrofit.create()
    }
}
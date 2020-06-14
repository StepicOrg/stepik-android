package org.stepik.android.view.injection.section

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.section.SectionCacheDataSourceImpl
import org.stepik.android.data.section.repository.SectionRepositoryImpl
import org.stepik.android.data.section.source.SectionCacheDataSource
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.remote.section.SectionRemoteDataSourceImpl
import org.stepik.android.remote.section.service.SectionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class SectionDataModule {
    @Binds
    internal abstract fun bindSectionRepository(
        sectionRepositoryImpl: SectionRepositoryImpl
    ): SectionRepository

    @Binds
    internal abstract fun bindSectionCacheDataSource(
        sectionCacheDataSourceImpl: SectionCacheDataSourceImpl
    ): SectionCacheDataSource

    @Binds
    internal abstract fun bindSectionRemoteDataSource(
        sectionRemoteDataSourceImpl: SectionRemoteDataSourceImpl
    ): SectionRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideSectionService(@Authorized retrofit: Retrofit): SectionService =
            retrofit.create(SectionService::class.java)
    }
}
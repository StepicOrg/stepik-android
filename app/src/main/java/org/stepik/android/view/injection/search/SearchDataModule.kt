package org.stepik.android.view.injection.search

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.search.SearchCacheDataSourceImpl
import org.stepik.android.data.search.repository.SearchRepositoryImpl
import org.stepik.android.data.search.source.SearchCacheDataSource
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.domain.search.repository.SearchRepository
import org.stepik.android.remote.search.SearchRemoteDataSourceImpl
import org.stepik.android.remote.search.service.SearchService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class SearchDataModule {
    @Binds
    internal abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    internal abstract fun bindSearchRemoteDataSource(
        searchRemoteDataSourceImpl: SearchRemoteDataSourceImpl
    ): SearchRemoteDataSource

    @Binds
    internal abstract fun bindSearchCacheDataSource(
        searchCacheDataSourceImpl: SearchCacheDataSourceImpl
    ): SearchCacheDataSource


    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideSearchService(@Authorized retrofit: Retrofit): SearchService =
            retrofit.create(SearchService::class.java)
    }
}
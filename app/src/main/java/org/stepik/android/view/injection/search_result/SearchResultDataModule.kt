package org.stepik.android.view.injection.search_result

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.search_result.repository.SearchResultRepositoryImpl
import org.stepik.android.data.search_result.source.SearchResultRemoteDataSource
import org.stepik.android.domain.search_result.repository.SearchResultRepository
import org.stepik.android.remote.search_result.SearchResultRemoteDataSourceImpl
import org.stepik.android.remote.search_result.service.SearchResultService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class SearchResultDataModule {
    @Binds
    internal abstract fun bindSearchResultRepository(
        searchResultRepositoryImpl: SearchResultRepositoryImpl
    ): SearchResultRepository

    @Binds
    internal abstract fun bindSearchResultRemoteDataSource(
        searchResultRemoteDataSourceImpl: SearchResultRemoteDataSourceImpl
    ): SearchResultRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideSearchResultService(@Authorized retrofit: Retrofit): SearchResultService =
            retrofit.create(SearchResultService::class.java)
    }
}
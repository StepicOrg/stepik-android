package org.stepik.android.view.injection.search

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.SearchSuggestionsPresenter
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.cache.search.SearchCacheDataSourceImpl
import org.stepik.android.data.search.source.SearchCacheDataSource
import org.stepik.android.domain.search.repository.SearchRepository

@Module
object CatalogSearchSuggestionsPresentationModule {
    @Provides
    internal fun provideSearchSuggestionsPresenter(
        searchRepository: SearchRepository,
        analytic: Analytic,
        @BackgroundScheduler
        scheduler: Scheduler,
        @MainScheduler
        mainScheduler: Scheduler
    ): SearchSuggestionsPresenter =
        SearchSuggestionsPresenter(
            courseId = -1L,
            searchRepository = searchRepository,
            analytic = analytic,
            scheduler = scheduler,
            mainScheduler = mainScheduler
        )

    @Provides
    internal fun provideSearchCacheDataSource(databaseFacade: DatabaseFacade): SearchCacheDataSource =
        SearchCacheDataSourceImpl(dbElementsCount = 2, databaseFacade = databaseFacade)
}
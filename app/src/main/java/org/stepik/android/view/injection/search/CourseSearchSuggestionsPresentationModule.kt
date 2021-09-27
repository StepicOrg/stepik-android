package org.stepik.android.view.injection.search

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.SearchSuggestionsPresenter
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.search.repository.SearchRepository

@Module
object CourseSearchSuggestionsPresentationModule {
    @Provides
    internal fun provideSearchSuggestionsPresenter(
        @CourseId
        courseId: Long,
        searchRepository: SearchRepository,
        analytic: Analytic,
        @BackgroundScheduler
        scheduler: Scheduler,
        @MainScheduler
        mainScheduler: Scheduler
    ): SearchSuggestionsPresenter =
        SearchSuggestionsPresenter(
            courseId = courseId,
            searchRepository = searchRepository,
            analytic = analytic,
            scheduler = scheduler,
            mainScheduler = mainScheduler
        )
}
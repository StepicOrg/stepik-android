package org.stepic.droid.features.stories.presentation

import android.content.res.Resources
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.stories.mapper.toStory
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepic.droid.util.defaultLocale
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.model.StoryTemplate
import org.stepik.android.presentation.catalog.model.CatalogItem
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class StoriesPresenter
@Inject
constructor(
        private val storiesRepository: StoryTemplatesRepository,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) : PresenterBase<StoriesView>(),
    CatalogItem {
    private var state : StoriesView.State = StoriesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        fetchStories()
    }

    fun fetchStories(forceUpdate: Boolean = false) {
        if (state != StoriesView.State.Idle && !forceUpdate) return
        state = StoriesView.State.Loading

        val locale = Resources.getSystem().configuration.defaultLocale

        compositeDisposable += Singles
            .zip(
                storiesRepository.getStoryTemplates(locale.language).map { it.map(StoryTemplate::toStory) },
                storiesRepository.getViewedStoriesIds()
            )
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { (stories, viewedIds) ->
                    state =
                        if (stories.isNotEmpty()) {
                            StoriesView.State.Success(stories, viewedIds)
                        } else {
                            StoriesView.State.Empty
                        }
                },
                onError = { state = StoriesView.State.Empty }
            )
    }

    override fun attachView(view: StoriesView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onStoryViewed(storyId: Long) {
        compositeDisposable += storiesRepository
            .markStoryAsViewed(storyId)
            .subscribeBy(
                onComplete = {
                    val state = this.state
                    if (state is StoriesView.State.Success) {
                        this.state = state.copy(viewedStoriesIds = state.viewedStoriesIds + storyId)
                    }
                },
                onError = emptyOnErrorStub
            )
    }
}
package org.stepic.droid.features.stories.presentation

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.stories.mapper.toStory
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepic.droid.util.addDisposable
import org.stepik.android.model.StoryTemplate
import javax.inject.Inject

class StoriesPresenter
@Inject
constructor(
        private val storiesRepository: StoryTemplatesRepository,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) : PresenterBase<StoriesView>() {
    private var state : StoriesView.State = StoriesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val compositeDisposable = CompositeDisposable()

    init {
        fetchStories()
    }

    private fun fetchStories() {
        if (state != StoriesView.State.Idle) return
        state = StoriesView.State.Loading

        compositeDisposable addDisposable
                zip(storiesRepository.getStoryTemplates().map { it.map(StoryTemplate::toStory) }, storiesRepository.getViewedStoriesIds())
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy({
                            state = StoriesView.State.Empty
                        }) { (stories, viewedIds) ->
                            state = StoriesView.State.Success(stories, viewedIds)
                        }
    }

    override fun attachView(view: StoriesView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onStoryViewed(storyId: Long) {
        compositeDisposable addDisposable storiesRepository
                .markStoryAsViewed(storyId)
                .subscribeBy({ /* ignore */ }) {
                    val state = this.state
                    if (state is StoriesView.State.Success) {
                        this.state = state.copy(viewedStoriesIds = state.viewedStoriesIds + storyId)
                    }
                }
    }
}
package org.stepik.android.presentation.stories.dispatcher

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
import org.stepik.android.model.StoryTemplate
import org.stepik.android.presentation.stories.StoriesFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StoriesActionDispatcher
@Inject
constructor(
    private val storiesRepository: StoryTemplatesRepository,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<StoriesFeature.Action, StoriesFeature.Message>() {
    override fun handleAction(action: StoriesFeature.Action) {
        when (action) {
            is StoriesFeature.Action.FetchStories -> {
                val locale = Resources.getSystem().configuration.defaultLocale

                compositeDisposable += Singles
                    .zip(
                        storiesRepository.getStoryTemplates(locale.language).map {
                            it.map(
                                StoryTemplate::toStory
                            )
                        },
                        storiesRepository.getViewedStoriesIds()
                    )
                    .map { (stories, viewedIds) ->
                        StoriesFeature.Message.FetchStoriesSuccess(
                            stories.sortedBy { if (it.id in viewedIds) 1 else 0 },
                            viewedIds
                        )
                    }
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(it) },
                        onError = { onNewMessage(StoriesFeature.Message.FetchStoriesError) }
                    )
            }
        }
    }
}
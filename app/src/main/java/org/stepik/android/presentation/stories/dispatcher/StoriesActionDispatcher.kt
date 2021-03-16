package org.stepik.android.presentation.stories.dispatcher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.stepik.android.domain.stories.interactor.StoriesInteractor
import org.stepik.android.presentation.stories.StoriesFeature
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StoriesActionDispatcher
@Inject
constructor(
    private val storiesInteractor: StoriesInteractor
) : RxActionDispatcher<StoriesFeature.Action, StoriesFeature.Message>() {
    private val dispatcherScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun handleAction(action: StoriesFeature.Action) {
        when (action) {
            is StoriesFeature.Action.FetchStories ->
                dispatcherScope.launch {
                    val message =
                        try {
                            withContext(Dispatchers.IO) {
                                val stories = async { storiesInteractor.fetchStories() }
                                val viewedIds = storiesInteractor.getViewedStoriesIds()

                                StoriesFeature.Message.FetchStoriesSuccess(
                                    stories.await().sortedBy { if (it.id in viewedIds) 1 else 0 },
                                    viewedIds
                                )
                            }
                        } catch (_: Exception) {
                            StoriesFeature.Message.FetchStoriesError
                        }
                    onNewMessage(message)
                }

            is StoriesFeature.Action.MarkStoryAsViewed ->
                dispatcherScope.launch {
                    withContext(Dispatchers.IO) {
                        storiesInteractor.markStoryAsViewed(action.storyId)
                    }
                }
        }
    }

    override fun cancel() {
        super.cancel()
        dispatcherScope.cancel()
    }
}
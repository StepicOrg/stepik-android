package org.stepik.android.data.story.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.story.source.StoryReactionCacheDataSource
import org.stepik.android.domain.story.model.StoryReaction
import org.stepik.android.domain.story.repository.StoryReactionRepository
import javax.inject.Inject

class StoryReactionRepositoryImpl
@Inject
constructor(
    private val storyReactionCacheDataSource: StoryReactionCacheDataSource
) : StoryReactionRepository {
    override fun getStoriesReactions(): Single<Map<Long, StoryReaction>> =
        storyReactionCacheDataSource.getStoriesReactions()

    override fun saveStoryReaction(storyId: Long, reaction: StoryReaction): Completable =
        storyReactionCacheDataSource.saveStoryReaction(storyId, reaction)
}
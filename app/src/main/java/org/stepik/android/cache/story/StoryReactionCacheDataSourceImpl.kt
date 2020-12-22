package org.stepik.android.cache.story

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.story.dao.StoryReactionDao
import org.stepik.android.cache.story.model.StoryReactionEntity
import org.stepik.android.data.story.source.StoryReactionCacheDataSource
import org.stepik.android.domain.story.model.StoryReaction
import javax.inject.Inject

class StoryReactionCacheDataSourceImpl
@Inject
constructor(
    private val storyReactionDao: StoryReactionDao
) : StoryReactionCacheDataSource {
    override fun getStoriesReactions(): Single<Map<Long, StoryReaction>> =
        storyReactionDao
            .getStoryReactions()
            .map { entities ->
                entities.associateBy(keySelector = StoryReactionEntity::storyId) { StoryReaction.valueOf(it.reaction) }
            }

    override fun saveStoryReaction(storyId: Long, reaction: StoryReaction): Completable =
        storyReactionDao.saveStoryReaction(StoryReactionEntity(storyId, reaction.name))
}
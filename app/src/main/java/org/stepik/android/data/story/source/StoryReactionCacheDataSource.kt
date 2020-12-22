package org.stepik.android.data.story.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.story.model.StoryReaction

interface StoryReactionCacheDataSource {
    fun getStoriesReactions(): Single<Map<Long, StoryReaction>>
    fun saveStoryReaction(storyId: Long, reaction: StoryReaction): Completable
}
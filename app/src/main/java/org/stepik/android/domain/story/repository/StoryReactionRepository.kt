package org.stepik.android.domain.story.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.story.model.StoryReaction

interface StoryReactionRepository {
    fun getStoriesReactions(): Single<Map<Long, StoryReaction>>
    fun saveStoryReaction(storyId: Long, reaction: StoryReaction): Completable
}
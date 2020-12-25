package org.stepik.android.domain.story.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.story.model.StoryReaction
import org.stepik.android.domain.story.repository.StoryReactionRepository
import javax.inject.Inject

class StoryReactionInteractor
@Inject
constructor(
    private val storyReactionRepository: StoryReactionRepository
) {
    fun getStoriesReactions(): Single<Map<Long, StoryReaction>> =
        storyReactionRepository.getStoriesReactions()

    fun saveStoryReaction(storyId: Long, reaction: StoryReaction): Completable =
        storyReactionRepository.saveStoryReaction(storyId, reaction)
}
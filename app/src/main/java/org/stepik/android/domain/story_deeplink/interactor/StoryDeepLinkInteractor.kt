package org.stepik.android.domain.story_deeplink.interactor

import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import javax.inject.Inject

class StoryDeepLinkInteractor
@Inject
constructor(
    private val storyTemplatesRepository: StoryTemplatesRepository
) {
//    fun getStoryTemplate(storyId: Long): Single<StoryTemplate> =
//        storyTemplatesRepository
//            .getStoryTemplate(storyId)
//            .doOnSuccess { storyTemplatesRepository.markStoryAsViewed(storyId) }
}
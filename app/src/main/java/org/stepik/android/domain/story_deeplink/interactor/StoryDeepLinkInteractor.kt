package org.stepik.android.domain.story_deeplink.interactor

import io.reactivex.Single
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepik.android.model.StoryTemplate
import javax.inject.Inject

class StoryDeepLinkInteractor
@Inject
constructor(
    private val storyTemplatesRepository: StoryTemplatesRepository
) {
    fun getStoryTemplate(storyId: Long): Single<StoryTemplate> =
        storyTemplatesRepository
            .getStoryTemplate(storyId)
            .doOnSuccess { storyTemplatesRepository.markStoryAsViewed(storyId) }
}
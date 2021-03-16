package org.stepik.android.domain.stories.interactor

import android.content.res.Resources
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.stepic.droid.features.stories.mapper.toStory
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepic.droid.util.defaultLocale
import org.stepik.android.domain.personal_offers.repository.PersonalOffersRepository
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.Story
import javax.inject.Inject

class StoriesInteractor
@Inject
constructor(
    private val personalOffersRepository: PersonalOffersRepository,
    private val storiesRepository: StoryTemplatesRepository
) {
    suspend fun fetchStories(): List<Story> =
        coroutineScope {
            val stories = async { getStoryTemplates() }
            val offerStories = async { getOfferStoryTemplates() }

            (stories.await() + offerStories.await())
                .sortedBy { it.position }
                .map(StoryTemplate::toStory)
        }

    suspend fun getViewedStoriesIds(): Set<Long> =
        storiesRepository.getViewedStoriesIds()

    suspend fun markStoryAsViewed(storyId: Long) {
        storiesRepository.markStoryAsViewed(storyId)
    }

    private suspend fun getStoryTemplates(): List<StoryTemplate> {
        val locale = Resources.getSystem().configuration.defaultLocale
        return storiesRepository.getStoryTemplates(locale.language)
    }

    private suspend fun getOfferStoryTemplates(): List<StoryTemplate> {
        val personalOffers = personalOffersRepository.getPersonalOffers()
        return storiesRepository.getStoryTemplates(personalOffers.promoStories.orEmpty())
    }
}
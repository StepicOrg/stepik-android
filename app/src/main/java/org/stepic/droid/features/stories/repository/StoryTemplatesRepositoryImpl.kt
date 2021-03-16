package org.stepic.droid.features.stories.repository

import org.stepic.droid.di.AppSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.model.StoryTemplate
import javax.inject.Inject

@AppSingleton
class StoryTemplatesRepositoryImpl
@Inject
constructor(
    private val storyTemplatesRemoteDataSource: StoryTemplatesRemoteDataSource,
    private val viewedStoryTemplateDao: IDao<ViewedStoryTemplate>
) : StoryTemplatesRepository {
    companion object {
        const val STORY_TEMPLATES_VERSION = 1
    }

    private val storyTemplatesMapper = { storyTemplates: List<StoryTemplate> ->
        storyTemplates
            .filter { template -> template.version <= STORY_TEMPLATES_VERSION }
    }

//    override fun getStoryTemplate(id: Long): Single<StoryTemplate> =
//        storyTemplatesRemoteDataSource
//            .getStoryTemplates(listOf(id))
//            .first()

    override suspend fun getStoryTemplates(ids: List<Long>): List<StoryTemplate> {
        if (ids.isEmpty()) return emptyList()

        return try {
            storyTemplatesRemoteDataSource
                .getStoryTemplates(ids)
                .let(storyTemplatesMapper)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getStoryTemplates(lang: String): List<StoryTemplate> =
        storyTemplatesRemoteDataSource
            .getStoryTemplates(lang)
            .let(storyTemplatesMapper)

    override suspend fun getViewedStoriesIds(): Set<Long> =
        viewedStoryTemplateDao
            .getAll()
            .asSequence()
            .map { it.storyTemplateId }
            .toSet()

    override suspend fun markStoryAsViewed(storyTemplateId: Long) {
        viewedStoryTemplateDao.insertOrReplace(ViewedStoryTemplate(storyTemplateId))
    }
}
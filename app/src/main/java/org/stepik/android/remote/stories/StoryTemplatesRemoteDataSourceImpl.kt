package org.stepik.android.remote.stories

import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.model.StoryTemplate
import org.stepik.android.remote.stories.service.StoryService
import javax.inject.Inject

class StoryTemplatesRemoteDataSourceImpl
@Inject
constructor(
    private val storyService: StoryService
) : StoryTemplatesRemoteDataSource {
    override suspend fun getStoryTemplates(ids: List<Long>): List<StoryTemplate> =
        storyService
            .getStoryTemplate(ids)
            .storyTemplates

    override suspend fun getStoryTemplates(lang: String): List<StoryTemplate> =
        getStoryTemplatesByPage(lang)

    private suspend fun getStoryTemplatesByPage(lang: String): List<StoryTemplate> {
        val items = arrayListOf<StoryTemplate>()
        var page = 1
        var hasNext = true
        while (hasNext) {
            val response =
                storyService.getStoryTemplate(page++, true, lang)
            items += response.storyTemplates
            hasNext = response.meta.hasNext
        }

        return items
    }
}
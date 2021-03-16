package org.stepik.android.data.stories.source

import org.stepik.android.model.StoryTemplate

interface StoryTemplatesRemoteDataSource {
    suspend fun getStoryTemplates(ids: List<Long>): List<StoryTemplate>
    suspend fun getStoryTemplates(lang: String): List<StoryTemplate>
}
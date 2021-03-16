package org.stepic.droid.features.stories.repository

import org.stepik.android.model.StoryTemplate

interface StoryTemplatesRepository {
//    fun getStoryTemplate(id: Long): Single<StoryTemplate>

    suspend fun getStoryTemplates(ids: List<Long>): List<StoryTemplate>

    suspend fun getStoryTemplates(lang: String): List<StoryTemplate>

    suspend fun getViewedStoriesIds(): Set<Long>

    suspend fun markStoryAsViewed(storyTemplateId: Long)
}
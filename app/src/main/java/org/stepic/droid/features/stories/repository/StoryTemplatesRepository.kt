package org.stepic.droid.features.stories.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.StoryTemplate

interface StoryTemplatesRepository {
    fun getStoryTemplates(lang: String): Single<List<StoryTemplate>>

    fun getViewedStoriesIds(): Single<Set<Long>>

    fun markStoryAsViewed(storyTemplateId: Long): Completable
}
package org.stepik.android.remote.stories

import android.content.res.Resources
import io.reactivex.Observable
import org.stepic.droid.util.defaultLocale
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.remote.stories.service.StoryService
import javax.inject.Inject

class StoryTemplatesRemoteRemoteDataSourceImpl
@Inject
constructor(
    private val storyService: StoryService
) : StoryTemplatesRemoteDataSource {
    override fun getStoryTemplates(page: Int): Observable<StoryTemplatesResponse> {
        val locale = Resources.getSystem().configuration.defaultLocale
        return storyService.getStoryTemplate(page, true, locale.language)
    }
}
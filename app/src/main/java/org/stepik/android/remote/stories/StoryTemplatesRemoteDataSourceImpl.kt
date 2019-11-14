package org.stepik.android.remote.stories

import android.content.res.Resources
import io.reactivex.Observable
import org.stepic.droid.util.defaultLocale
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.remote.stories.model.StoryTemplatesResponse
import org.stepik.android.remote.stories.service.StoryService
import javax.inject.Inject

class StoryTemplatesRemoteDataSourceImpl
@Inject
constructor(
    private val storyService: StoryService
) : StoryTemplatesRemoteDataSource {
    override fun getStoryTemplates(page: Int): Observable<StoryTemplatesResponse> {
        val locale = Resources.getSystem().configuration.defaultLocale
        return storyService.getStoryTemplate(page, true, locale.language)
    }
}
package org.stepik.android.data.stories.source

import io.reactivex.Observable
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse

interface StoryTemplatesRemoteDataSource {
    fun getStoryTemplates(page: Int): Observable<StoryTemplatesResponse>
}
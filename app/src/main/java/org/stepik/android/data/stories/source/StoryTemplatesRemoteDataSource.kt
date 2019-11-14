package org.stepik.android.data.stories.source

import io.reactivex.Observable
import org.stepik.android.remote.stories.model.StoryTemplatesResponse

interface StoryTemplatesRemoteDataSource {
    fun getStoryTemplates(page: Int): Observable<StoryTemplatesResponse>
}
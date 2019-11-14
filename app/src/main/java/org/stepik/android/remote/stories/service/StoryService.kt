package org.stepik.android.remote.stories.service

import io.reactivex.Observable
import org.stepik.android.remote.stories.model.StoryTemplatesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StoryService {
    @GET("api/story-templates")
    fun getStoryTemplate(
        @Query("page") page: Int,
        @Query("is_published") isPublished: Boolean,
        @Query("language") language: String
    ): Observable<StoryTemplatesResponse>
}
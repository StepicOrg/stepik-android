package org.stepik.android.remote.tags.service

import io.reactivex.Single
import org.stepik.android.remote.tags.model.TagResponse
import retrofit2.http.GET

interface TagsService {
    @GET("api/tags?is_featured=true")
    fun getFeaturedTags(): Single<TagResponse>
}
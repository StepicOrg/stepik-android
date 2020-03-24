package org.stepik.android.remote.tags.service

import io.reactivex.Single
import org.stepik.android.remote.search.model.SearchResultResponse
import org.stepik.android.remote.tags.model.TagResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface TagsService {
    @GET("api/tags?is_featured=true")
    fun getFeaturedTags(): Single<TagResponse>

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    fun getSearchResultsOfTag(@QueryMap query: Map<String, String>): Single<SearchResultResponse>
}
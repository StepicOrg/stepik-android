package org.stepik.android.remote.tags.service

import io.reactivex.Single
import org.stepic.droid.web.SearchResultResponse
import org.stepic.droid.web.TagResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TagsService {
    @GET("api/tags?is_featured=true")
    fun getFeaturedTags(): Single<TagResponse>

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    fun getSearchResultsOfTag(
        @Query("page") page: Int,
        @Query("tag") id: Int,
        @Query("language") lang: String
    ): Single<SearchResultResponse>
}
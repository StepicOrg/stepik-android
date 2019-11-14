package org.stepik.android.data.tags.source

import io.reactivex.Single
import org.stepik.android.model.Tag
import org.stepik.android.remote.search.model.SearchResultResponse
import org.stepik.android.remote.tags.model.TagResponse

interface TagsRemoteDataSource {
    fun getFeaturedTags(): Single<TagResponse>
    fun getSearchResultsOfTag(page: Int, tag: Tag): Single<SearchResultResponse>
}
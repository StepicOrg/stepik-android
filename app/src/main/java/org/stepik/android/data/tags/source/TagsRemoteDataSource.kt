package org.stepik.android.data.tags.source

import io.reactivex.Single
import org.stepic.droid.web.SearchResultResponse
import org.stepic.droid.web.TagResponse
import org.stepik.android.model.Tag

interface TagsRemoteDataSource {
    fun getFeaturedTags(): Single<TagResponse>
    fun getSearchResultsOfTag(page: Int, tag: Tag): Single<SearchResultResponse>
}
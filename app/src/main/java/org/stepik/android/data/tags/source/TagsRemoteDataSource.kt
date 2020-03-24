package org.stepik.android.data.tags.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.SearchQuery
import org.stepik.android.model.SearchResult
import org.stepik.android.model.Tag

interface TagsRemoteDataSource {
    fun getFeaturedTags(): Single<List<Tag>>
    fun getSearchResultsOfTag(searchQuery: SearchQuery): Single<PagedList<SearchResult>>
}
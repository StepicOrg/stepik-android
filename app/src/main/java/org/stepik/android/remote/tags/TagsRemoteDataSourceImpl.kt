package org.stepik.android.remote.tags

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.tags.source.TagsRemoteDataSource
import org.stepik.android.domain.course_list.model.SearchQuery
import org.stepik.android.model.SearchResult
import org.stepik.android.model.Tag
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.search.mapper.SearchQueryMapper
import org.stepik.android.remote.search.model.SearchResultResponse
import org.stepik.android.remote.tags.model.TagResponse
import org.stepik.android.remote.tags.service.TagsService
import javax.inject.Inject

class TagsRemoteDataSourceImpl
@Inject
constructor(
    private val tagsService: TagsService,
    private val searchQueryMapper: SearchQueryMapper
) : TagsRemoteDataSource {
    override fun getFeaturedTags(): Single<List<Tag>> =
        tagsService.getFeaturedTags().map(TagResponse::tags)

    override fun getSearchResultsOfTag(searchQuery: SearchQuery): Single<PagedList<SearchResult>> =
        tagsService
            .getSearchResultsOfTag(searchQueryMapper.mapToQueryMap(searchQuery))
            .map { it.toPagedList(SearchResultResponse::searchResultList) }
}
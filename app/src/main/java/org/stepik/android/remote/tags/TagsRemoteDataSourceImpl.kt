package org.stepik.android.remote.tags

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.tags.source.TagsRemoteDataSource
import org.stepik.android.model.SearchResult
import org.stepik.android.model.Tag
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.search.model.SearchResultResponse
import org.stepik.android.remote.tags.model.TagResponse
import org.stepik.android.remote.tags.service.TagsService
import javax.inject.Inject

class TagsRemoteDataSourceImpl
@Inject
constructor(
    private val tagsService: TagsService
) : TagsRemoteDataSource {
    override fun getFeaturedTags(): Single<List<Tag>> =
        tagsService.getFeaturedTags().map(TagResponse::tags)

    override fun getSearchResultsOfTag(page: Int, tag: Tag, lang: String): Single<PagedList<SearchResult>> =
        tagsService
            .getSearchResultsOfTag(page, tag.id, lang)
            .map { it.toPagedList(SearchResultResponse::searchResultList) }
}
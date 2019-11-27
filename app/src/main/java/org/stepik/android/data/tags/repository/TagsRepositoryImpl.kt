package org.stepik.android.data.tags.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.tags.source.TagsRemoteDataSource
import org.stepik.android.domain.tags.repository.TagsRepository
import org.stepik.android.model.SearchResult
import org.stepik.android.model.Tag
import javax.inject.Inject

class TagsRepositoryImpl
@Inject
constructor(
    private val tagsRemoteDataSource: TagsRemoteDataSource
) : TagsRepository {
    override fun getFeaturedTags(): Single<List<Tag>> =
        tagsRemoteDataSource.getFeaturedTags()

    override fun getSearchResultsOfTag(page: Int, tag: Tag, lang: String): Single<PagedList<SearchResult>> =
        tagsRemoteDataSource.getSearchResultsOfTag(page, tag, lang)
}
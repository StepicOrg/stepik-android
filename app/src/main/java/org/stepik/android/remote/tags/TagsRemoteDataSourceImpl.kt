package org.stepik.android.remote.tags

import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.SearchResultResponse
import org.stepic.droid.web.TagResponse
import org.stepik.android.data.tags.source.TagsRemoteDataSource
import org.stepik.android.model.Tag
import org.stepik.android.remote.tags.service.TagsService
import javax.inject.Inject

class TagsRemoteDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val tagsService: TagsService
) : TagsRemoteDataSource {
    override fun getFeaturedTags(): Single<TagResponse> =
        tagsService.getFeaturedTags()

    override fun getSearchResultsOfTag(page: Int, tag: Tag): Single<SearchResultResponse> {
        val enumSet = sharedPreferenceHelper.filterForFeatured
        val lang = enumSet.iterator().next().language
        return tagsService.getSearchResultsOfTag(page, tag.id, lang)
    }
}
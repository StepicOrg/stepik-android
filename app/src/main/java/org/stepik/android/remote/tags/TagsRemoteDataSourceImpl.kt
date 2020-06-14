package org.stepik.android.remote.tags

import io.reactivex.Single
import org.stepik.android.data.tags.source.TagsRemoteDataSource
import org.stepik.android.model.Tag
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
}
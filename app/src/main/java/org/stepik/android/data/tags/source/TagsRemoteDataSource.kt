package org.stepik.android.data.tags.source

import io.reactivex.Single
import org.stepik.android.model.Tag

interface TagsRemoteDataSource {
    fun getFeaturedTags(): Single<List<Tag>>
}
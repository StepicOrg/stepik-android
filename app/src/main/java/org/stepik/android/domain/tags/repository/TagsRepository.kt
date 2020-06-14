package org.stepik.android.domain.tags.repository

import io.reactivex.Single
import org.stepik.android.model.Tag

interface TagsRepository {
    fun getFeaturedTags(): Single<List<Tag>>
}
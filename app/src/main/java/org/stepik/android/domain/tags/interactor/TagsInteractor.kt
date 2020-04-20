package org.stepik.android.domain.tags.interactor

import io.reactivex.Single
import org.stepik.android.domain.tags.repository.TagsRepository
import org.stepik.android.model.Tag
import javax.inject.Inject

class TagsInteractor
@Inject
constructor(
    private val tagsRepository: TagsRepository
) {
    fun fetchFeaturedTags(): Single<List<Tag>> =
        tagsRepository.getFeaturedTags()
}
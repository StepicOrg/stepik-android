package org.stepik.android.domain.comment_banner.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.comment_banner.repository.CommentBannerRepository
import javax.inject.Inject

class CommentBannerInteractor
@Inject
constructor(
    private val commentBannerRepository: CommentBannerRepository
) {
    fun shouldShowCommentsBannerForCourse(courseId: Long): Single<Boolean> =
        commentBannerRepository.hasCourseId(courseId)

    fun onBannerShown(courseId: Long): Completable =
        commentBannerRepository.addCourseId(courseId)
}
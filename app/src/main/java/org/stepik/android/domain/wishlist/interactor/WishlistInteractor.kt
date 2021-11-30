package org.stepik.android.domain.wishlist.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistEntry
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.presentation.wishlist.model.WishlistAction
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import javax.inject.Inject

class WishlistInteractor
@Inject
constructor(
    private val wishlistRepository: WishlistRepository,
    @WishlistOperationBus
    private val wishlistOperationPublisher: PublishSubject<WishlistOperationData>
) {
    fun getWishlist(dataSourceType: DataSourceType = DataSourceType.CACHE): Single<List<Long>> =
        wishlistRepository
            .getWishlistEntries(dataSourceType)
            .map { wishlistEntries -> wishlistEntries.map(WishlistEntry::course) }

    fun updateWishlistWithOperation(wishlistOperationData: WishlistOperationData): Completable =
        getWishlistOperationSource(wishlistOperationData)
            .doOnComplete { wishlistOperationPublisher.onNext(wishlistOperationData) }

    private fun getWishlistOperationSource(wishlistOperationData: WishlistOperationData): Completable =
        if (wishlistOperationData.wishlistAction == WishlistAction.ADD) {
            wishlistRepository.addCourseToWishlist(wishlistOperationData.courseId)
        } else {
            wishlistRepository.removeCourseFromWishlist(wishlistOperationData.courseId, sourceType = DataSourceType.REMOTE)
        }
}
package org.stepik.android.domain.wishlist.interactor

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import javax.inject.Inject

class WishlistInteractor
@Inject
constructor(
    private val wishlistRepository: WishlistRepository,

    @WishlistOperationBus
    private val wishlistOperationPublisher: PublishSubject<WishlistOperationData>
) {
    fun getWishlist(dataSourceType: DataSourceType = DataSourceType.CACHE): Single<WishlistEntity> =
        wishlistRepository
            .getWishlistRecord(dataSourceType)

    fun updateWishlistWithOperation(wishlistEntity: WishlistEntity, wishlistOperationData: WishlistOperationData): Single<WishlistEntity> =
        wishlistRepository
            .updateWishlistRecord(wishlistEntity)
            .doOnSuccess { wishlistOperationPublisher.onNext(wishlistOperationData) }

    fun updateWishlist(wishlistEntity: WishlistEntity): Single<WishlistEntity> =
        wishlistRepository
            .updateWishlistRecord(wishlistEntity)
}
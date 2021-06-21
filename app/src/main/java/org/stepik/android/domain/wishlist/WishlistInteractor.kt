package org.stepik.android.domain.wishlist

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepik.android.cache.wishlist.mapper.WishlistEntityMapper
import org.stepik.android.cache.wishlist.model.WishlistEntity
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import javax.inject.Inject

class WishlistInteractor
@Inject
constructor(
    private val wishlistRepository: WishlistRepository,
    private val wishlistEntityMapper: WishlistEntityMapper,

    @WishlistOperationBus
    private val wishlistOperationPublisher: PublishSubject<WishlistOperationData>
) {
    fun getWishlist(dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<WishlistEntity> =
        wishlistRepository
            .getWishlistRecord(dataSourceType)
            .map(wishlistEntityMapper::mapToEntity)

    fun updateWishlistRecord(wishlistEntity: WishlistEntity, wishlistOperationData: WishlistOperationData): Single<WishlistEntity> =
        wishlistRepository
            .updateWishlistRecord(wishlistEntityMapper.mapToStorageRecord(wishlistEntity))
            .map(wishlistEntityMapper::mapToEntity)
            .doOnSuccess { wishlistOperationPublisher.onNext(wishlistOperationData) }
}
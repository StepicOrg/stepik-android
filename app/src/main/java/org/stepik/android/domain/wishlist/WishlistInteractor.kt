package org.stepik.android.domain.wishlist

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.cache.wishlist.mapper.WishlistEntityMapper
import org.stepik.android.cache.wishlist.model.WishlistEntity
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.view.injection.course_list.WishlistOperationBus
import javax.inject.Inject

class WishlistInteractor
@Inject
constructor(
    private val wishlistRepository: WishlistRepository,
    private val wishlistEntityMapper: WishlistEntityMapper,

    @WishlistOperationBus
    private val wishlistOperationPublisher: PublishSubject<Long>
) {
    fun getWishlist(dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<StorageRecord<WishlistWrapper>> =
        wishlistRepository.getWishlistRecord(dataSourceType)

    fun updateWishlistRecord(wishlistEntity: WishlistEntity, updatedCourseId: Long): Single<WishlistEntity> =
        wishlistRepository
            .updateWishlistRecord(wishlistEntityMapper.mapToStorageRecord(wishlistEntity))
            .map(wishlistEntityMapper::mapToEntity)
            .doOnSuccess { wishlistOperationPublisher.onNext(updatedCourseId) }
}
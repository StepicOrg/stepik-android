package org.stepik.android.remote.wishlist

import io.reactivex.Single
import org.stepik.android.data.wishlist.KIND_WISHLIST
import org.stepik.android.data.wishlist.source.WishlistRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.remote.wishlist.model.WishlistWrapper
import org.stepik.android.remote.remote_storage.service.RemoteStorageService
import org.stepik.android.remote.wishlist.mapper.WishlistMapper
import ru.nobird.android.domain.rx.toMaybe
import javax.inject.Inject

class WishlistRemoteDataSourceImpl
@Inject
constructor(
    private val profileRepository: ProfileRepository,
    private val remoteStorageService: RemoteStorageService,
    private val wishlistMapper: WishlistMapper
) : WishlistRemoteDataSource {

    override fun getWishlistRecord(): Single<WishlistEntity> =
        profileRepository
            .getProfile(primarySourceType = DataSourceType.REMOTE)
            .flatMap { profile ->
                remoteStorageService
                    .getStorageRecords(1, profile.id, kind = KIND_WISHLIST)
                    .flatMapMaybe { response ->
                        wishlistMapper
                            .mapToEntity(response)
                            .toMaybe()
                    }
                    .switchIfEmpty(createWishlistRecord(WishlistWrapper.EMPTY))
            }

    override fun createWishlistRecord(wishlistWrapper: WishlistWrapper): Single<WishlistEntity> =
        remoteStorageService
            .createStorageRecord(
                wishlistMapper.mapToStorageRequest(wishlistWrapper)
            )
            .map(wishlistMapper::mapToEntity)

    override fun updateWishlistRecord(wishlistEntity: WishlistEntity): Single<WishlistEntity> =
        remoteStorageService
            .setStorageRecord(wishlistEntity.recordId, wishlistMapper.mapToStorageRequest(wishlistEntity))
            .map(wishlistMapper::mapToEntity)
}
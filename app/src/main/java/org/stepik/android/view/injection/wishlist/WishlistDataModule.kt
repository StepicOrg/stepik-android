package org.stepik.android.view.injection.wishlist

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.wishlist.WishlistCacheDataSourceImpl
import org.stepik.android.data.wishlist.repository.WishlistRepositoryImpl
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.data.wishlist.source.WishlistRemoteDataSource
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.remote.wishlist.WishlistRemoteDataSourceImpl
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.remote_storage.RemoteStorageDataModule

@Module(includes = [ProfileDataModule::class, RemoteStorageDataModule::class])
abstract class WishlistDataModule {
    @Binds
    internal abstract fun bindWishlistRepository(
        wishlistRepositoryImpl: WishlistRepositoryImpl
    ): WishlistRepository

    @Binds
    internal abstract fun bindWishlistRemoteDataSource(
        wishlistRemoteDataSourceImpl: WishlistRemoteDataSourceImpl
    ): WishlistRemoteDataSource

    @Binds
    internal abstract fun bindWishlistCacheDataSource(
        wishlistCacheDataSourceImpl: WishlistCacheDataSourceImpl
    ): WishlistCacheDataSource
}
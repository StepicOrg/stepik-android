package org.stepik.android.view.injection.wishlist

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.wishlist.WishlistCacheDataSourceImpl
import org.stepik.android.cache.wishlist.dao.WishlistDao
import org.stepik.android.data.wishlist.repository.WishlistRepositoryImpl
import org.stepik.android.data.wishlist.source.WishlistCacheDataSource
import org.stepik.android.data.wishlist.source.WishlistRemoteDataSource
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.remote.wishlist.WishlistRemoteDataSourceImpl
import org.stepik.android.remote.wishlist.WishlistService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideWishlistDao(appDatabase: AppDatabase): WishlistDao =
            appDatabase.wishlistDao()

        @Provides
        @JvmStatic
        internal fun provideWishlistService(@Authorized retrofit: Retrofit): WishlistService =
            retrofit.create()
    }
}
package org.stepik.android.remote.wishlist

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.wishlist.model.WishlistRequest
import org.stepik.android.remote.wishlist.model.WishlistResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WishlistService {
    @GET("api/wish-lists")
    fun getWishlist(@Query("page") page: Int): Single<WishlistResponse>

    @POST("api/wish-lists")
    fun updateWishlist(@Body body: WishlistRequest): Single<WishlistResponse>

    @DELETE("api/wish-lists/{wishlistEntryId}")
    fun removeWishlistEntry(@Path("wishlistEntryId") wishlistEntryId: Long): Completable
}
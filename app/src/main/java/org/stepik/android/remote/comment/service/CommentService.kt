package org.stepik.android.remote.comment.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.comment.model.CommentRequest
import org.stepik.android.remote.comment.model.CommentResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentService {
    @GET("api/comments")
    fun getComments(@Query("ids[]") ids: LongArray): Single<CommentResponse>

    @POST("api/comments")
    fun createComment(@Body request: CommentRequest): Single<CommentResponse>

    @PUT("api/comments/{commentId}")
    fun saveComment(@Path("commentId") commentId: Long, @Body request: CommentRequest): Single<CommentResponse>

    @DELETE("api/comments/{commentId}")
    fun removeComment(@Path("commentId") commentId: Long): Completable
}
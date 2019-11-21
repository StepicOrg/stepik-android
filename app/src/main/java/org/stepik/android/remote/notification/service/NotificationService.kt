package org.stepik.android.remote.notification.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.notification.model.NotificationRequest
import org.stepik.android.remote.notification.model.NotificationResponse
import org.stepik.android.remote.notification.model.NotificationStatusesResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationService {
    @PUT("api/notifications/{id}")
    fun putNotification(
        @Path("id") notificationId: Long,
        @Body notificationRequest: NotificationRequest
    ): Completable

    @GET("api/notifications")
    fun getNotifications(
        @Query("page") page: Int,
        @Query("type") type: String?
    ): Single<NotificationResponse>

    @FormUrlEncoded
    @POST("api/notifications/mark-as-read")
    fun markNotificationAsRead(
        @Field(value = "type", encoded = true) notificationType: String?
    ): Completable

    @GET("api/notification-statuses")
    fun getNotificationStatuses(): Single<NotificationStatusesResponse>
}
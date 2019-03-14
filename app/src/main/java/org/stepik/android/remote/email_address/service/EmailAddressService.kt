package org.stepik.android.remote.email_address.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.email_address.model.EmailAddressRequest
import org.stepik.android.remote.email_address.model.EmailAddressResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmailAddressService {
    @GET("api/email-addresses")
    fun getEmailAddresses(
        @Query("ids[]") ids: LongArray
    ): Single<EmailAddressResponse>

    @POST("api/email-addresses")
    fun createEmailAddress(
        @Body request: EmailAddressRequest
    ): Single<EmailAddressResponse>

    @POST("api/email-addresses/{emailId}/set-as-primary")
    fun setPrimaryEmailAddress(
        @Path("emailId") emailId: Long
    ): Completable

    @DELETE("api/email-addresses/{emailId}")
    fun removeEmailAddress(
        @Path("emailId") emailId: Long
    ): Completable
}
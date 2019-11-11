package org.stepik.android.data.email_address.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.user.EmailAddress
import org.stepik.android.remote.email_address.model.EmailAddressResponse

interface EmailAddressRemoteDataSource {
    fun getEmailAddresses(vararg emailIds: Long): Single<List<EmailAddress>>

    fun createEmailAddress(emailAddress: EmailAddress): Single<EmailAddress>

    fun setPrimaryEmailAddress(emailId: Long): Completable

    fun removeEmailAddress(emailId: Long): Completable

    fun getEmailAddressesResponse(vararg emailIds: Long): Single<EmailAddressResponse>
}
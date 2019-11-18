package org.stepik.android.data.email_address.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.user.EmailAddress

interface EmailAddressRemoteDataSource {
    fun getEmailAddresses(vararg emailIds: Long): Single<List<EmailAddress>>

    fun createEmailAddress(emailAddress: EmailAddress): Single<EmailAddress>

    fun setPrimaryEmailAddress(emailId: Long): Completable

    fun removeEmailAddress(emailId: Long): Completable
}
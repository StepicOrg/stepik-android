package org.stepik.android.data.email_address.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.user.EmailAddress

interface EmailAddressCacheDataSource {
    fun getEmailAddresses(vararg emailIds: Long): Single<List<EmailAddress>>

    fun saveEmailAddress(emailAddress: EmailAddress): Completable =
        saveEmailAddresses(listOf(emailAddress))

    fun saveEmailAddresses(emailAddresses: List<EmailAddress>): Completable

    fun removeEmailAddress(emailId: Long): Completable
}
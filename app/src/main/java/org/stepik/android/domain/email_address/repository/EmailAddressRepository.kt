package org.stepik.android.domain.email_address.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.user.EmailAddress

interface EmailAddressRepository {
    /**
     * Returns email addresses with given ids from primary and secondary data sources
     */
    fun getEmailAddresses(vararg emailIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<EmailAddress>>

    /**
     * Creates new email address and returns created email address object
     */
    fun createEmailAddress(emailAddress: EmailAddress): Single<EmailAddress>

    /**
     * Marks email address with given emailId as primary
     */
    fun setPrimaryEmailAddress(emailId: Long): Completable

    /**
     * Removes given email address
     */
    fun removeEmailAddress(emailId: Long): Completable
}
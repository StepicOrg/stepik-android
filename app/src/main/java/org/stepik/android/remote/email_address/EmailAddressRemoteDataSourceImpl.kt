package org.stepik.android.remote.email_address

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.email_address.source.EmailAddressRemoteDataSource
import org.stepik.android.model.user.EmailAddress
import org.stepik.android.remote.email_address.model.EmailAddressRequest
import org.stepik.android.remote.email_address.model.EmailAddressResponse
import org.stepik.android.remote.email_address.service.EmailAddressService
import javax.inject.Inject

class EmailAddressRemoteDataSourceImpl
@Inject
constructor(
    private val emailAddressService: EmailAddressService
) : EmailAddressRemoteDataSource {
    override fun getEmailAddresses(vararg emailIds: Long): Single<List<EmailAddress>> =
        emailAddressService
            .getEmailAddresses(emailIds)
            .map(EmailAddressResponse::emailAddresses)

    override fun createEmailAddress(emailAddress: EmailAddress): Single<EmailAddress> =
        emailAddressService
            .createEmailAddress(EmailAddressRequest(emailAddress))
            .map { it.emailAddresses.first() }

    override fun setPrimaryEmailAddress(emailId: Long): Completable =
        emailAddressService
            .setPrimaryEmailAddress(emailId)

    override fun removeEmailAddress(emailId: Long): Completable =
        emailAddressService
            .removeEmailAddress(emailId)

    override fun getEmailAddressesResponse(vararg emailIds: Long): Single<EmailAddressResponse> =
        emailAddressService
            .getEmailAddresses(emailIds)
}
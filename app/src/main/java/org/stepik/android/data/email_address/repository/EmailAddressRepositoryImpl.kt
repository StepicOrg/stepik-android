package org.stepik.android.data.email_address.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
import org.stepik.android.data.email_address.source.EmailAddressCacheDataSource
import org.stepik.android.data.email_address.source.EmailAddressRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.email_address.repository.EmailAddressRepository
import org.stepik.android.model.user.EmailAddress
import javax.inject.Inject

class EmailAddressRepositoryImpl
@Inject
constructor(
    private val remoteDataSource: EmailAddressRemoteDataSource,
    private val cacheDataSource: EmailAddressCacheDataSource
) : EmailAddressRepository {

    override fun getEmailAddresses(vararg emailIds: Long, primarySourceType: DataSourceType): Single<List<EmailAddress>> {
        val remoteSource = remoteDataSource
            .getEmailAddresses(*emailIds)
            .doCompletableOnSuccess(cacheDataSource::saveEmailAddresses)

        val cacheSource = cacheDataSource
            .getEmailAddresses(*emailIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(emailIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedEmails ->
                    val ids = (emailIds.toList() - cachedEmails.map(EmailAddress::id)).toLongArray()
                    remoteDataSource
                        .getEmailAddresses(*ids)
                        .doCompletableOnSuccess(cacheDataSource::saveEmailAddresses)
                        .map { remoteEmails -> cachedEmails + remoteEmails }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { emailAddresses -> emailAddresses.sortedBy { emailIds.indexOf(it.id) } }
    }

    override fun createEmailAddress(emailAddress: EmailAddress): Single<EmailAddress> =
        remoteDataSource
            .createEmailAddress(emailAddress)
            .doCompletableOnSuccess(cacheDataSource::saveEmailAddress)

    override fun setPrimaryEmailAddress(emailId: Long): Completable =
        remoteDataSource
            .setPrimaryEmailAddress(emailId)

    override fun removeEmailAddress(emailId: Long): Completable =
        remoteDataSource
            .removeEmailAddress(emailId)
            .andThen(cacheDataSource.removeEmailAddress(emailId))
}
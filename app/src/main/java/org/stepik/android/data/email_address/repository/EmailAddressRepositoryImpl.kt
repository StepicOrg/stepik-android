package org.stepik.android.data.email_address.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.email_address.source.EmailAddressCacheDataSource
import org.stepik.android.data.email_address.source.EmailAddressRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.email_address.repository.EmailAddressRepository
import org.stepik.android.model.user.EmailAddress
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class EmailAddressRepositoryImpl
@Inject
constructor(
    private val remoteDataSource: EmailAddressRemoteDataSource,
    private val cacheDataSource: EmailAddressCacheDataSource
) : EmailAddressRepository {
    private val delegate =
        ListRepositoryDelegate(
            remoteDataSource::getEmailAddresses,
            cacheDataSource::getEmailAddresses,
            cacheDataSource::saveEmailAddresses
        )

    override fun getEmailAddresses(emailIds: List<Long>, primarySourceType: DataSourceType): Single<List<EmailAddress>> =
        delegate.get(emailIds, primarySourceType, allowFallback = true)

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
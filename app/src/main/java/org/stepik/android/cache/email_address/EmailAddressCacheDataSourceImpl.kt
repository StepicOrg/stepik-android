package org.stepik.android.cache.email_address

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.data.email_address.source.EmailAddressCacheDataSource
import org.stepik.android.model.user.EmailAddress
import javax.inject.Inject

class EmailAddressCacheDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : EmailAddressCacheDataSource {
    override fun getEmailAddresses(emailIds: List<Long>): Single<List<EmailAddress>> =
        Single.fromCallable {
            sharedPreferenceHelper.storedEmails
                ?.filter { it.id in emailIds }
                ?.sortedBy { emailIds.indexOf(it.id) }
                .orEmpty()
        }

    override fun saveEmailAddresses(emailAddresses: List<EmailAddress>): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.storeEmailAddresses(emailAddresses)
        }

    override fun removeEmailAddress(emailId: Long): Completable =
        Completable.fromAction {
            val storedEmails = sharedPreferenceHelper.storedEmails
            val removed = storedEmails?.removeAll { it.id == emailId }
            if (removed == true) {
                sharedPreferenceHelper.storeEmailAddresses(storedEmails)
            }
        }
}
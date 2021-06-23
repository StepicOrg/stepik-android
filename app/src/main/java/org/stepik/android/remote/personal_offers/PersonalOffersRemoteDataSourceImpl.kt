package org.stepik.android.remote.personal_offers

import com.google.gson.JsonObject
import io.reactivex.Scheduler
import io.reactivex.Single
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.PersonalOffersScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepik.android.data.personal_offers.source.PersonalOffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.PersonalOffers
import org.stepik.android.remote.personal_offers.mapper.PersonalOffersMapper
import org.stepik.android.remote.remote_storage.model.StorageRequest
import org.stepik.android.remote.remote_storage.service.RemoteStorageService
import ru.nobird.android.domain.rx.toMaybe
import javax.inject.Inject

@AppSingleton
class PersonalOffersRemoteDataSourceImpl
@Inject
constructor(
    private val remoteStorageService: RemoteStorageService,
    private val personalOffersMapper: PersonalOffersMapper,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @PersonalOffersScheduler
    private val scheduler: Scheduler
) : PersonalOffersRemoteDataSource {
    companion object {
        private const val KIND_PERSONAL_OFFERS = "personal_offers"
    }
    override fun getPersonalOffers(): Single<PersonalOffers> =
        Single
            .fromCallable { sharedPreferenceHelper.profile?.id ?: -1 }
            .flatMap { userId ->
                remoteStorageService
                    .getStorageRecords(page = 1, userId = userId, kind = KIND_PERSONAL_OFFERS)
                    .flatMapMaybe {
                        personalOffersMapper
                            .mapToPersonalOffers(it)
                            .toMaybe()
                    }
                    .switchIfEmpty(createPersonalOffers())
                    .subscribeOn(scheduler)
            }

    override fun createPersonalOffers(): Single<PersonalOffers> =
        remoteStorageService
            .createStorageRecord(
                StorageRequest(
                    StorageRecordWrapped(
                        id = null,
                        kind = KIND_PERSONAL_OFFERS,
                        data = JsonObject()
                    )
                )
            )
            .map(personalOffersMapper::mapToPersonalOffers)
}
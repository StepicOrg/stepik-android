package org.stepik.android.data.mobile_tiers.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.mobile_tiers.model.MobileTier

interface MobileTiersCacheDataSource {
    fun getMobileTiers(courseIds: List<Long>): Single<List<MobileTier>>
    fun saveMobileTiers(items: List<MobileTier>): Completable
}
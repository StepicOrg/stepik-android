package org.stepik.android.domain.mobile_tiers.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import ru.nobird.android.domain.rx.maybeFirst

interface MobileTiersRepository {
    fun calculateMobileTier(mobileTierCalculation: MobileTierCalculation, dataSourceType: DataSourceType = DataSourceType.REMOTE): Maybe<MobileTier> =
        calculateMobileTiers(listOf(mobileTierCalculation), dataSourceType = dataSourceType).maybeFirst()

    fun calculateMobileTiers(mobileTierCalculations: List<MobileTierCalculation>, dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<List<MobileTier>>
}
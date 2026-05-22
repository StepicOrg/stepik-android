package org.stepik.android.data.rubricator.source

import io.reactivex.Single
import org.stepik.android.domain.rubricator.model.RubricatorData

interface RubricatorRemoteDataSource {
    fun getRubricatorData(rubricatorUrl: String): Single<RubricatorData>
}
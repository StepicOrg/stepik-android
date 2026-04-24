package org.stepik.android.domain.rubricator.repository

import io.reactivex.Single
import org.stepik.android.domain.rubricator.model.RubricatorData

interface RubricatorRepository {
    fun getRubricatorData(rubricatorUrl: String): Single<RubricatorData>
}
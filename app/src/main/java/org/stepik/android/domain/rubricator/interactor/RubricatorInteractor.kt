package org.stepik.android.domain.rubricator.interactor

import io.reactivex.Single
import org.stepik.android.domain.rubricator.model.RubricatorData
import org.stepik.android.domain.rubricator.repository.RubricatorRepository
import javax.inject.Inject

class RubricatorInteractor
@Inject
constructor(
    private val rubricatorRepository: RubricatorRepository
) {
    fun getRubricatorData(rubricatorUrl: String): Single<RubricatorData> =
        if (rubricatorUrl.isEmpty()) {
            Single.just(RubricatorData.EMPTY)
        } else {
            rubricatorRepository.getRubricatorData(rubricatorUrl)
        }
}
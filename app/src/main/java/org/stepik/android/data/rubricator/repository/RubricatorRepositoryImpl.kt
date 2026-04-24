package org.stepik.android.data.rubricator.repository

import io.reactivex.Single
import org.stepik.android.data.rubricator.source.RubricatorRemoteDataSource
import org.stepik.android.domain.rubricator.model.RubricatorData
import org.stepik.android.domain.rubricator.repository.RubricatorRepository
import javax.inject.Inject

class RubricatorRepositoryImpl @Inject constructor(
    private val rubricatorRemoteDataSource: RubricatorRemoteDataSource
) : RubricatorRepository {
    override fun getRubricatorData(rubricatorUrl: String): Single<RubricatorData> =
        rubricatorRemoteDataSource.getRubricatorData(rubricatorUrl)
}
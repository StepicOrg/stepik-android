package org.stepik.android.remote.rubricator

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.rubricator.source.RubricatorRemoteDataSource
import org.stepik.android.domain.rubricator.model.RubricatorData
import org.stepik.android.remote.rubricator.model.RubricatorResponse
import org.stepik.android.remote.rubricator.service.RubricatorService
import javax.inject.Inject

class RubricatorRemoteDataSourceImpl @Inject constructor(
    private val rubricatorService: RubricatorService
) : RubricatorRemoteDataSource {
    private val mapper = Function { response: RubricatorResponse ->
        RubricatorData(
            response.subjects,
            response.metaCategories,
            response.courseLists
        )
    }
    override fun getRubricatorData(rubricatorUrl: String): Single<RubricatorData> =
        rubricatorService
            .getRubricatorData(rubricatorUrl)
            .map(mapper)
}
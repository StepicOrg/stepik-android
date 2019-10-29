package org.stepik.android.data.step_source.repository

import io.reactivex.Single
import org.stepik.android.data.step_source.source.StepSourceRemoteDataSource
import org.stepik.android.domain.step_source.repository.StepSourceRepository
import org.stepik.android.model.StepSource
import javax.inject.Inject

class StepSourceRepositoryImpl
@Inject
constructor(
    private val stepSourceRemoteDataSource: StepSourceRemoteDataSource
) : StepSourceRepository {
    override fun getStepSource(stepId: Long): Single<StepSource> =
        stepSourceRemoteDataSource
            .getStepSource(stepId)

    override fun saveStepSource(stepSource: StepSource): Single<StepSource> =
        stepSourceRemoteDataSource
            .saveStepSource(stepSource)
}
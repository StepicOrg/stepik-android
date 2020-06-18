package org.stepik.android.domain.step.repository

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Step

interface StepRepository {
    fun getStep(stepId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Step> =
        getSteps(stepId, primarySourceType = primarySourceType).maybeFirst()

    fun getSteps(vararg stepIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Step>>

    fun getStepByLessonId(lessonId: Long): Single<Step>
}
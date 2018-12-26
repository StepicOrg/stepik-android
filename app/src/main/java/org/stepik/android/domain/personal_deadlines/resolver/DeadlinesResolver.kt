package org.stepik.android.domain.personal_deadlines.resolver

import io.reactivex.Single
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.domain.personal_deadlines.model.LearningRate

interface DeadlinesResolver {
    fun calculateDeadlinesForCourse(courseId: Long, learningRate: LearningRate): Single<DeadlinesWrapper>
}
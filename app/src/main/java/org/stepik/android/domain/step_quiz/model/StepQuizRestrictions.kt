package org.stepik.android.domain.step_quiz.model

import org.stepik.android.model.DiscountingPolicyType

data class StepQuizRestrictions(
    val submissionCount: Int,
    val maxSubmissionCount: Int,
    val discountingPolicyType: DiscountingPolicyType
)
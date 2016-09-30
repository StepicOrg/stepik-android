package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.DiscountingPolicyType

interface StepAttemptView {
    fun onResultHandlingDiscountPolicy(needShow: Boolean, discountingPolicyType: DiscountingPolicyType? = null, remainTries: Int = -1)
}

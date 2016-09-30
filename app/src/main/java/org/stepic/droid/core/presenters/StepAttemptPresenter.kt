package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.StepAttemptView
import org.stepic.droid.model.DiscountingPolicyType
import org.stepic.droid.model.Section

class StepAttemptPresenter : PresenterBase<StepAttemptView>() {


    fun handleDiscountingPolicy(numberOfSubmission: Int, section: Section?) {
        if (section?.discountingPolicy == null || section?.discountingPolicy == DiscountingPolicyType.noDiscount || numberOfSubmission < 0) {
            view?.onResultHandlingDiscountPolicy(needShow = false)
            return
        }

        section?.discountingPolicy?.let {
            when (section.discountingPolicy) {
                DiscountingPolicyType.inverse -> view?.onResultHandlingDiscountPolicy(
                        needShow = true,
                        discountingPolicyType = it,
                        remainTries = Int.MAX_VALUE)

                DiscountingPolicyType.firstOne, DiscountingPolicyType.firstThree -> view?.onResultHandlingDiscountPolicy(
                        needShow = true,
                        discountingPolicyType = it,
                        remainTries = (it.numberOfTries() - numberOfSubmission))

                else -> view?.onResultHandlingDiscountPolicy(needShow = false)
            }
        }
    }

}

package org.stepic.droid.core.presenters.contracts

import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Submission

interface StepAttemptView {
    fun onResultHandlingDiscountPolicy(needShow: Boolean, discountingPolicyType: DiscountingPolicyType? = null, remainTries: Int = -1)

    fun onStartLoadingAttempt()

    fun onNeedShowAttempt(attempt: Attempt?, isCreated: Boolean, numberOfSubmissionsForStep: Int?)

    fun onConnectionFailWhenLoadAttempt()

    fun onNeedFillSubmission(submission: Submission?, numberOfSubmissions: Int)

    fun onConnectionFailOnSubmit()

    fun onNeedShowPeerReview()

    fun onNeedResolveActionButtonText()

    fun onResultHandlingSubmissionRestriction(needShow: Boolean, numberForShow: Int)

    fun onNeedShowStreakDialog(streakDays: Int)

    fun onNeedShowRateDialog()

    fun sendTextFeedback(supportEmailData: SupportEmailData)

}
package org.stepik.android.presentation.feedback

import org.stepik.android.domain.feedback.model.SupportEmailData

interface FeedbackView {
    fun sendTextFeedback(supportEmailData: SupportEmailData)
}
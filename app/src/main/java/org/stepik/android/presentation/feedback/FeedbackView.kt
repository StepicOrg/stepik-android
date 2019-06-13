package org.stepik.android.presentation.feedback

import org.stepik.android.domain.feedback.model.EmailUriData

interface FeedbackView {
    fun sendTextFeedback(emailUriData: EmailUriData)
}
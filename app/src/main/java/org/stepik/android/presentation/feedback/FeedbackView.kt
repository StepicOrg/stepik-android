package org.stepik.android.presentation.feedback

interface FeedbackView {
    fun sendTextFeedback(mailTo: String, subject: String, body: String)
}
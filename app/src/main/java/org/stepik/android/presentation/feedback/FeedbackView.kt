package org.stepik.android.presentation.feedback

interface FeedbackView {
    fun sendTextFeedback(mailTo: String, subject: String, body: String)
    fun getMailToString(): String
    fun getEmailSubjectString(): String
    fun getAboutSystemInfo(): String
}
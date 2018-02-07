package org.stepic.droid.adaptive.listeners

interface AnswerListener {
    fun onCorrectAnswer(submissionId: Long)
    fun onWrongAnswer(submissionId: Long)
}
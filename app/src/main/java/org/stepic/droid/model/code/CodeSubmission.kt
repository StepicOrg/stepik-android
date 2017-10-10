package org.stepic.droid.model.code

data class CodeSubmission(val attemptId: Long,
                          val stepId: Long,
                          val language: String,
                          val code: String)

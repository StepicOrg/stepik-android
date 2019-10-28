package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.feedback.ChoiceFeedback
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class SubmissionTest {
    companion object {
        fun createTestSubmission(): Submission =
            Submission(
                id = 111,
                status = Submission.Status.LOCAL,
                score = "score",
                hint = "hint",
                time = "time",
                _reply = ReplyTest.createTestReply(),
                attempt = 999,
                session = "session",
                eta = "eta",
                feedback = ChoiceFeedback(listOf("a", "b"))
            )
    }

    @Test
    fun submissionIsParcelable() {
        createTestSubmission()
            .assertThatObjectParcelable<Submission>()
    }
}
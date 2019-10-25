package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class ReplyTest {
    companion object {
        fun createTestReply(): Reply =
            Reply(
                choices = listOf(true, false),
                text = "text",
                attachments = listOf(Attachment("name", 999, "url", "content", "type")),
                formula = "formula",
                number = "number",
                ordering = listOf(1, 2, 3),
                language = "language",
                code = "code",
                solveSql = "solve sql"
            )
    }

    @Test
    fun replyIsSerializable() {
        createTestReply()
            .assertThatObjectParcelable<Reply>()
    }
}
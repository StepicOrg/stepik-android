package org.stepic.droid.model

import com.google.gson.Gson
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.TestingGsonProvider
import org.stepik.android.remote.comment.model.CommentResponse

@RunWith(RobolectricTestRunner::class)
class CommentResponseTest {

    private val gson: Gson = TestingGsonProvider.gson

    @Test
    fun parsedAllFieldsJson() {
        val json = """{"attempts": [],
            "comments": [],
            "submissions": [],
            "users": [],
            "votes": []
            }
        """.trimIndent()

        val commentResponse = gson.fromJson(json, CommentResponse::class.java)

        assertNotNull(commentResponse)
        assertNotNull(commentResponse.attempts)
        assertNotNull(commentResponse.submissions)
        assertNotNull(commentResponse.users)
        assertNotNull(commentResponse.votes)
    }
}
package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CommentTest {
    @Test
    fun commentIsParcelable() {
        val comment = Comment(replies = listOf(1, 2, 3))
        comment.assertThatObjectParcelable<Comment>()
    }
}
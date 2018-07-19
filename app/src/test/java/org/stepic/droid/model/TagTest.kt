package org.stepic.droid.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.model.Tag

@RunWith(RobolectricTestRunner::class)
class TagTest {

    @Test
    fun tagIsParcelable() {
        val tag = Tag(id = 42, title = "Math")
        tag.assertThatObjectParcelable<Tag>()
    }
}

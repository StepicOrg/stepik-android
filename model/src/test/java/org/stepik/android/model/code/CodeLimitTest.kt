package org.stepik.android.model.code

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable
import org.stepik.android.model.code.CodeLimit

@RunWith(RobolectricTestRunner::class)
class CodeLimitTest {

    @Test
    fun simpleLimit_success() {
        val limit = CodeLimit(255, 1)
        limit.assertThatObjectParcelable<CodeLimit>()
    }
}

package org.stepic.droid.model.code

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.model.structure.code.CodeLimit

@RunWith(RobolectricTestRunner::class)
class CodeLimitTest {

    @Test
    fun simpleLimit_success() {
        val limit = CodeLimit(255, 1)
        limit.assertThatObjectParcelable<CodeLimit>()
    }
}

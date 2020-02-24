package org.stepic.droid.model.code

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.TestingGsonProvider
import org.stepik.android.model.code.CodeOptions

@RunWith(RobolectricTestRunner::class)
class CodeOptionsTest {
    private val gson = TestingGsonProvider.gson

    @Test
    fun emptyCodeOptionsNull() {
        val optionsJson = "{}"
        val options = gson.fromJson(optionsJson, CodeOptions::class.java)

        Assert.assertNotNull(options)
    }
}

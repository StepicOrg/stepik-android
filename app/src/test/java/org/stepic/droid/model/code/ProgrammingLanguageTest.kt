@file:Suppress("DEPRECATION") //suppress for testing

package org.stepic.droid.model.code

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class ProgrammingLanguageTest {

    @Test
    fun noWhitespaces_success() {
        ProgrammingLanguage.PYTHON.assertThatObjectParcelable<ProgrammingLanguage>()
    }

    @Test
    fun whitespaceInName_success() {
        ProgrammingLanguage.CS.assertThatObjectParcelable<ProgrammingLanguage>()
    }
}

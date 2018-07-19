package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.code.CodeLimit
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class SectionTest {
    @Test
    fun emptySectionParcelable() {
        val section = Section()
        section.assertThatObjectParcelable<CodeLimit>()
    }


    @Test
    fun notEmptySectionParcelable() {
        val section = Section(id = 233, units = listOf(1, 2, 3), position = 2)
        section.assertThatObjectParcelable<Section>()
    }
}
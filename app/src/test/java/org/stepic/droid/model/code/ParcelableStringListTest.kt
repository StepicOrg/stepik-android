package org.stepic.droid.model.code

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable
import org.stepik.android.model.util.ParcelableStringList

@RunWith(RobolectricTestRunner::class)
class ParcelableStringListTest {
    @Test
    fun emptyList_success() {
        val list = ParcelableStringList()
        list.assertThatObjectParcelable<ParcelableStringList>()
    }

    @Test
    fun singleElement_success() {
        val list = ParcelableStringList()
        list.add("Hello")
        list.assertThatObjectParcelable<ParcelableStringList>()
    }

    @Test
    fun manyElements_success() {
        val list = ParcelableStringList()
        list.add("One")
        list.add("Two")
        list.add("Three")
        list.assertThatObjectParcelable<ParcelableStringList>()
    }
}

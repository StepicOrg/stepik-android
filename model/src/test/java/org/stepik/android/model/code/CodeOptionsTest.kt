package org.stepik.android.model.code

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.ParcelableStringList
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CodeOptionsTest {
    @Test
    fun simpleCodeOptionsSuccess() {
        val sample1 = ParcelableStringList().apply {
            addAll(listOf("1", "3"))
        }
        val sample2 = ParcelableStringList().apply {
            addAll(listOf("1", "2", "3", "4"))
        }
        val codeOptions = CodeOptions(
                limits = mapOf("java" to CodeLimit(3, 256)),
                executionMemoryLimit = 120,
                executionTimeLimit = 5,
                codeTemplates = hashMapOf("java" to "public static etc"),
                samples = listOf(sample1, sample2),
                isRunUserCodeAllowed = true
        )

        codeOptions.assertThatObjectParcelable<CodeOptions>()
    }


    @Test
    fun emptyListsAndMapsSuccess() {
        val codeOptions = CodeOptions(
                limits = emptyMap(),
                executionTimeLimit = 0,
                executionMemoryLimit = 0,
                codeTemplates = emptyMap(),
                samples = emptyList(),
                isRunUserCodeAllowed = false
        )

        codeOptions.assertThatObjectParcelable<CodeOptions>()
    }
}
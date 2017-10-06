package org.stepic.droid.model.code

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CodeOptionsTest {

    @Test
    fun simpleCodeOptions_success() {
        val sample1 = ParcelableStringList().apply {
            addAll(listOf("1", "3"))
        }
        val sample2 = ParcelableStringList().apply {
            addAll(listOf("1", "2", "3", "4"))
        }
        val codeOptions = CodeOptions(
                limits = mapOf(ProgrammingLanguage.JAVA to CodeLimit(3, 256)),
                executionMemoryLimit = 120,
                executionTimeLimit = 5,
                codeTemplates = hashMapOf(ProgrammingLanguage.JAVA to "public static etc"),
                samples = listOf(sample1, sample2)
        )

        codeOptions.assertThatObjectParcelable<CodeOptions>()
    }


    @Test
    fun emptyListsAndMaps_success() {
        val codeOptions = CodeOptions(
                limits = emptyMap<ProgrammingLanguage, CodeLimit>(),
                executionTimeLimit = 0,
                executionMemoryLimit = 0,
                codeTemplates = HashMap<ProgrammingLanguage, String>(),
                samples = emptyList()
        )

        codeOptions.assertThatObjectParcelable<CodeOptions>()
    }
}

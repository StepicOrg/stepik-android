package org.stepik.android.ktlint.rules

import org.junit.Test

class ExplicitPublicTypeRuleTest {
    private val rule = ExplicitPublicTypeRule()

    @Test
    fun `public expression body function without type`() {
        assertLintErrors(
            """
                fun test() = "Hello"
            """,
            rule,
            2 to 17
        )
    }

    @Test
    fun `public expression body function with type`() {
        assertNoLintErrors(
            """
                fun test(): String = "Hello"
            """,
            rule
        )
    }

    @Test
    fun `private expression body function without type`() {
        assertNoLintErrors(
            """
                private fun test() = "Hello"
            """,
            rule
        )
    }

    @Test
    fun `private expression body function with type`() {
        assertNoLintErrors(
            """
                private fun test(): String = "Hello"
            """,
            rule
        )
    }

    @Test
    fun `public regular expression body function`() {
        assertNoLintErrors(
            """
                private fun test() {
                    "Hello"
                }
            """,
            rule
        )
    }
}
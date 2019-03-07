package org.stepik.android.ktlint.rules

import org.junit.Test

class ExpressionBodyIndentRuleTest {

    private val rule = ExpressionBodyIndentRule()

    @Test
    fun `no continuation indent should be at expression body function`() {
        assertLintErrors(
            """
                fun test(): String =
                        "Hello"
            """,
            rule,
            2 to 17
        )
    }

    @Test
    fun `expression body function longer than 10 symbols should be at next line`() {
        assertLintErrors(
            """
                fun test(): String = "12345678910"
            """,
            rule,
            2 to 17
        )
    }

    @Test
    fun `expression body function less or equal than 10 symbols may be at current line`() {
        assertNoLintErrors(
            """
                fun test(): String = "12345678"
            """,
            rule
        )
    }

    @Test
    fun `expression body function less or equal than 10 symbols may be at next line`() {
        assertNoLintErrors(
            """
                fun test(): String =
                    "12345678"
            """,
            rule
        )
    }

    @Test
    fun `long expression body function should be at next line`() {
        assertNoLintErrors(
            """
                fun test(): String =
                    Single.handleGenericError()
            """,
            rule
        )
    }
}
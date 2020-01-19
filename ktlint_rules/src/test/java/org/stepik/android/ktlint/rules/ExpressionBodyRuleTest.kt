package org.stepik.android.ktlint.rules

import org.junit.Test

class ExpressionBodyRuleTest {
    private val rule = ExpressionBodyRule()

    @Test
    fun `single line statements should be marked as expression body`() {
        assertLintErrors(
            """
                private fun hello(): String {
                    return "hello"
                }
            """,
            rule,
            2 to 17
        )
    }

    @Test
    fun `single line statements should be marked as expression body format`() {
        assertFormat(
            """
                private fun hello(): String {
                    return "hello"
                }
            """,
            """
                private fun hello(): String = 
                    "hello"
            """,
            rule
        )
    }

    @Test
    fun `single line statements should be marked as expression body format with random whitespaces`() {
        assertFormat(
            """
                private fun hello(): String {
                    return                 "hello"
                }
            """,
            """
                private fun hello(): String = 
                    "hello"
            """,
            rule
        )
    }
}
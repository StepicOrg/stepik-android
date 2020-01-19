package org.stepik.android.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.test.format
import com.pinterest.ktlint.test.lint
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals

fun assertNoLintErrors(@Language("kotlin") code: String, rule: Rule) {
    assert(rule.lint(code).isEmpty()) { "There should't be an lint errors with rule ${rule.id} for: \n$code" }
}

fun assertLintErrors(@Language("kotlin") code: String, rule: Rule, vararg errors: Pair<Int, Int>) {
    assertEquals(errors.toList(), rule.lint(code).map { it.line to it.col })
}

fun assertFormat(@Language("kotlin") code: String, @Language("kotlin") expectedCode: String, rule: Rule) {
    assertEquals(expectedCode, rule.format(code))
}
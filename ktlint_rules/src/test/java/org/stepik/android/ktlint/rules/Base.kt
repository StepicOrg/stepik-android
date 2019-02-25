package org.stepik.android.ktlint.rules

import com.github.shyiko.ktlint.core.Rule
import com.github.shyiko.ktlint.test.lint
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals

fun assertNoLintErrors(@Language("kotlin") code: String, rule: Rule) {
    assert(rule.lint(code).isEmpty()) { "There should't be an lint errors with rule ${rule.id} for: \n$code" }
}

fun assertLintErrors(@Language("kotlin") code: String, rule: Rule, vararg errors: Pair<Int, Int>) {
    assertEquals(errors.toList(), rule.lint(code).map { it.line to it.col })
}
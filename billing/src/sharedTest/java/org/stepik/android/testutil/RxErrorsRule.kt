package org.stepik.android.testutil

import io.reactivex.plugins.RxJavaPlugins
import org.junit.Assert.assertTrue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/** Installs a per-test global error trap and resets it even after an assertion failure. */
class RxErrorsRule : TestRule {
    private val errors = mutableListOf<Throwable>()

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            errors.clear()
            RxJavaPlugins.setErrorHandler { errors.add(it) }
            try {
                base.evaluate()
                assertTrue("Undeliverable Rx errors: $errors", errors.isEmpty())
            } finally {
                RxJavaPlugins.reset()
            }
        }
    }
}

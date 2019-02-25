package org.stepik.android.ktlint.rules

import com.github.shyiko.ktlint.core.RuleSet
import com.github.shyiko.ktlint.core.RuleSetProvider

class StepikRuleSetProvider : RuleSetProvider {
    override fun get(): RuleSet =
        RuleSet(
            "stepik-ktlint-rules",
            ExpressionBodyRule(),
            ExpressionBodyIndentRule(),
            ExplicitPublicTypeRule()
        )
}
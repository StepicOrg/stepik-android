package org.stepik.android.ktlint.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class StepikRuleSetProvider : RuleSetProvider {
    override fun get(): RuleSet =
        RuleSet(
            "stepik-ktlint-rules",
            ExpressionBodyRule(),
            ExpressionBodyIndentRule(),
            ExplicitPublicTypeRule()
        )
}
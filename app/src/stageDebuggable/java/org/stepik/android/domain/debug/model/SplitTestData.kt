package org.stepik.android.domain.debug.model

import ru.nobird.app.core.model.Identifiable

data class SplitTestData(
    val splitTestName: String,
    val splitTestValue: String,
    val splitTestGroups: List<String>
) : Identifiable<String> {
    override val id: String =
        splitTestName
}
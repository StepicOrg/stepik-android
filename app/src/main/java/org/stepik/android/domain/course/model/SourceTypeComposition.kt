package org.stepik.android.domain.course.model

import org.stepik.android.domain.base.DataSourceType

data class SourceTypeComposition(
    val generalSourceType: DataSourceType,
    val enrollmentSourceType: DataSourceType = generalSourceType
) {
    companion object {
        val REMOTE = SourceTypeComposition(DataSourceType.REMOTE)
        val CACHE = SourceTypeComposition(DataSourceType.CACHE)
    }
}
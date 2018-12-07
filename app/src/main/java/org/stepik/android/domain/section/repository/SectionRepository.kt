package org.stepik.android.domain.section.repository

import io.reactivex.Maybe
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Section

interface SectionRepository {
    fun getSection(sectionId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Section>
}
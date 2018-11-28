package org.stepik.android.domain.section.repository

import io.reactivex.Maybe
import org.stepik.android.model.Section

interface SectionRepository {
    fun getSection(sectionId: Long): Maybe<Section>
}
package org.stepik.android.data.section.source

import io.reactivex.Single
import org.stepik.android.model.Section

interface SectionRemoteDataSource {
    fun getSections(sectionIds: List<Long>): Single<List<Section>>
}
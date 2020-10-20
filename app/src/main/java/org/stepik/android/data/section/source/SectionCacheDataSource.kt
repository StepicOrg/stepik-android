package org.stepik.android.data.section.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Section

interface SectionCacheDataSource {
    fun getSections(sectionIds: List<Long>): Single<List<Section>>

    fun saveSections(sections: List<Section>): Completable
}
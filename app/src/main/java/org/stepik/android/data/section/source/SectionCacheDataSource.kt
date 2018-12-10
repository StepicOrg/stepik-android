package org.stepik.android.data.section.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.model.Section

interface SectionCacheDataSource {
    fun getSection(sectionId: Long): Maybe<Section>
    fun getSections(vararg sectionIds: Long): Single<List<Section>>

    fun saveSection(section: Section): Completable
    fun saveSections(sections: List<Section>): Completable
}
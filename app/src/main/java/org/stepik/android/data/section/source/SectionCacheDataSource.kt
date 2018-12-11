package org.stepik.android.data.section.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Section

interface SectionCacheDataSource {
    fun getSection(sectionId: Long): Maybe<Section> =
        getSections(sectionId).maybeFirst()

    fun getSections(vararg sectionIds: Long): Single<List<Section>>

    fun saveSection(section: Section): Completable =
        saveSections(listOf(section))

    fun saveSections(sections: List<Section>): Completable
}
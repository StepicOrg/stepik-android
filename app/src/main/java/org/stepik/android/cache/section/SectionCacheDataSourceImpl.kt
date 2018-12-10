package org.stepik.android.cache.section

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.maybeFirst
import org.stepik.android.data.section.source.SectionCacheDataSource
import org.stepik.android.model.Section
import javax.inject.Inject

class SectionCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : SectionCacheDataSource {
    override fun getSection(sectionId: Long): Maybe<Section> =
        getSections(sectionId)
            .maybeFirst()

    override fun getSections(vararg sectionIds: Long): Single<List<Section>> =
        Single.fromCallable {
            databaseFacade.getSectionsByIds(sectionIds)
        }

    override fun saveSection(section: Section): Completable =
        saveSections(listOf(section))

    override fun saveSections(sections: List<Section>): Completable =
        Completable.fromAction {
            databaseFacade.addSections(sections)
        }
}
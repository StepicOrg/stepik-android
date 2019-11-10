package org.stepic.droid.storage.repositories.section

import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.data.section.source.SectionRemoteDataSource
import org.stepik.android.model.Section
import javax.inject.Inject

class SectionRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val sectionRemoteDataSource: SectionRemoteDataSource
) : Repository<Section> {

    override fun getObjects(keys: LongArray): Iterable<Section> {
        var sections = databaseFacade.getSectionsByIds(keys)
        if (sections.size != keys.size) {
            sections =
                    try {
                        sectionRemoteDataSource.getSections(*keys).execute()?.body()?.sections?.also {
                            it.forEach(databaseFacade::addSection)
                        } ?: emptyList()
                    } catch (exception: Exception) {
                        emptyList()
                    }
        }
        sections = sections.sortedBy { it.position }
        return sections
    }


    override fun getObject(key: Long): Section? {
        var section = databaseFacade.getSectionById(key) // can be substitute in abstraction with IDao<Section>
        if (section == null) {
            section =
                    try {
                        sectionRemoteDataSource.getSections(key).execute()
                                ?.body()
                                ?.sections
                                ?.firstOrNull()
                                ?.also(databaseFacade::addSection)
                    } catch (exception: Exception) {
                        null
                    }
        }
        return section
    }

}

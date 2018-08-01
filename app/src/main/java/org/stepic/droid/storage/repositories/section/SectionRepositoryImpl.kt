package org.stepic.droid.storage.repositories.section

import org.stepik.android.model.Section
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import javax.inject.Inject

class SectionRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : Repository<Section> {

    override fun getObjects(keys: LongArray): Iterable<Section> {
        var sections = databaseFacade.getSectionsByIds(keys)
        if (sections.size != keys.size) {
            sections =
                    try {
                        api.getSections(keys).execute()?.body()?.sections?.also {
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
                        api.getSections(longArrayOf(key)).execute()
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

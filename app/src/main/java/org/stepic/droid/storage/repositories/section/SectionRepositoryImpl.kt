package org.stepic.droid.storage.repositories.section

import org.stepic.droid.model.Section
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import javax.inject.Inject

class SectionRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : Repository<Section, Long> {
    override fun getObjects(keys: Array<Long>): Iterable<Section> {
        var sections = databaseFacade.getSectionsByIds(keys.toLongArray())
        if (sections.size != keys.size) {
            sections =
                    try {
                        api.getSections(keys.toLongArray()).execute()?.body()?.sections ?: ArrayList<Section>()
                    } catch (exception: Exception) {
                        ArrayList<Section>()
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
                        api.getSections(longArrayOf(key)).execute()?.body()?.sections?.firstOrNull()
                    } catch (exception: Exception) {
                        null
                    }
        }
        return section
    }

}

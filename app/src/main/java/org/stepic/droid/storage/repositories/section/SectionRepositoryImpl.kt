package org.stepic.droid.storage.repositories.section

import org.stepic.droid.model.Section
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.IRepository
import org.stepic.droid.web.Api
import javax.inject.Inject

class SectionRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : IRepository<Section, Long> {


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

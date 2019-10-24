package org.stepic.droid.persistence.content

import io.reactivex.Single
import org.stepic.droid.persistence.content.processors.StepContentProcessor
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.persistence.storage.PersistentItemObserver
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepik.android.model.Step
import javax.inject.Inject

@PersistenceScope
class StepContentResolverImpl
@Inject
constructor(
    private val processors: Set<@JvmSuppressWildcards StepContentProcessor>,

    private val persistentItemDao: PersistentItemDao,
    private val persistentItemObserver: PersistentItemObserver,
    private val externalStorageManager: ExternalStorageManager
): StepContentResolver {
    override fun getDownloadableContentFromStep(step: Step, configuration: DownloadConfiguration): Set<String> =
        processors.map { it.extractDownloadableContent(step, configuration) }.reduce { a, b -> a union b }

    override fun resolvePersistentContent(step: Step): Single<StepPersistentWrapper> =
        persistentItemDao
            .getItems(mapOf(
                DBStructurePersistentItem.Columns.STEP to step.id.toString(),
                DBStructurePersistentItem.Columns.STATUS to PersistentItem.Status.COMPLETED.name
            ))
            .map { items ->
                val linkMap = resolveLinksMap(items)
                processors.fold(StepPersistentWrapper(step)) { wrapper, processor ->
                    processor.injectPersistentContent(wrapper, linkMap)
                }
            }

    private fun resolveLinksMap(items: List<PersistentItem>): Map<String, String> {
        val map = mutableMapOf<String, String>()

        items.forEach { item ->
            val originalPath = item.task.originalPath
            val localPath = externalStorageManager.resolvePathForPersistentItem(item)

            if (localPath == null) {
                persistentItemObserver.update(item.copy(status = PersistentItem.Status.CANCELLED))
            } else {
                map[originalPath] = localPath
            }
        }

        return map
    }
}
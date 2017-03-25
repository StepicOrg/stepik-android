package org.stepic.droid.store

import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.RWLocks
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class SectionDownloaderImpl
@Inject constructor(private val databaseFacade: DatabaseFacade,
                    private val downloadManager: IDownloadManager,
                    private val threadPoolExecutor: ThreadPoolExecutor,
                    private val cleanManager: CleanManager,
                    private val cancelSniffer: CancelSniffer) : SectionDownloader {

    override fun downloadSection(sectionId: Long) {
        threadPoolExecutor.execute {
            val section = databaseFacade.getSectionById(sectionId)
            cancelSniffer.removeSectionIdCancel(sectionId)
            downloadManager.addSection(section)
        }
    }

    override fun cancelSectionLoading(sectionId: Long) {
        threadPoolExecutor.execute {
            try {
                RWLocks.SectionCancelLock.writeLock().lock()
                cancelSniffer.addSectionIdCancel(sectionId)

                val units = databaseFacade.getAllUnitsOfSection(sectionId).filterNotNull()

                //units can be null if they are not loaded from internet -> steps are not loaded too and will be cancelled, when step downloading will executed
                if (units.isNotEmpty()) {
                    val lessonIds = LongArray(units.size)
                    for (i in units.indices) {
                        lessonIds[i] = units[i].lesson
                    }
                    val lessons = databaseFacade.getLessonsByIds(lessonIds).filterNotNull()
                    if (lessons.isNotEmpty()) {
                        lessons.forEach { lesson ->
                            lesson.steps?.forEach {
                                cancelSniffer.addStepIdCancel(it)
                            }
                            lesson.steps?.forEach {
                                downloadManager.cancelStep(it)
                            }
                        }
                    }
                }
            } finally {
                RWLocks.SectionCancelLock.writeLock().unlock()
            }

        }
    }

    override fun deleteWholeSection(sectionId: Long) {
        threadPoolExecutor.execute {
            cleanManager.removeSection(sectionId)
        }
    }

}

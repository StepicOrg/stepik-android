package org.stepic.droid.persistence.downloads.helpers

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.Structure

interface AddDownloadTaskHelper {
    fun addTasks(structureObservable: Observable<Structure>, configuration: DownloadConfiguration): Completable
}
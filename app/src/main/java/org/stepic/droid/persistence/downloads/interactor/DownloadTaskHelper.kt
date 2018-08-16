package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.Structure

interface DownloadTaskHelper {
    fun addTasks(
            structureObservable: Observable<Structure>,
            configuration: DownloadConfiguration
    ): Completable

    fun removeTasks(
            structureObservable: Observable<Structure>,
            persistentItemsObservable: Observable<List<PersistentItem>>
    ): Completable
}
package org.stepic.droid.persistence.downloads.helpers

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepic.droid.persistence.model.Structure

interface RemoveDownloadTaskHelper {
    fun removeTasks(structureObservable: Observable<Structure>): Completable
}
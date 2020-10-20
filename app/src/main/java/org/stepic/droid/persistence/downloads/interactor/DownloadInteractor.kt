package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadConfiguration

interface DownloadInteractor<T> {
    fun addTask(ids: List<Long>, configuration: DownloadConfiguration): Completable
    fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable

    fun removeTask(ids: List<Long>): Completable
    fun removeTask(vararg item: T): Completable
}
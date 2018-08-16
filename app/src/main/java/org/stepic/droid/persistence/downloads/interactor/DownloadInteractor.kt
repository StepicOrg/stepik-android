package org.stepic.droid.persistence.downloads.interactor

import io.reactivex.Completable
import org.stepic.droid.persistence.model.DownloadConfiguration

interface DownloadInteractor<T> {
    fun addTask(vararg ids: Long, configuration: DownloadConfiguration): Completable
    fun addTask(vararg items: T, configuration: DownloadConfiguration): Completable

    fun removeTask(vararg id: Long): Completable
    fun removeTask(vararg item: T): Completable
}
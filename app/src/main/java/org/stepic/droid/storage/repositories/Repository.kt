package org.stepic.droid.storage.repositories

import androidx.annotation.WorkerThread

/**
 * T – type,
 * K – type of Key
 */
interface Repository<T> {

    @WorkerThread
    fun getObject(key: Long): T?

    @WorkerThread
    fun getObjects(keys: LongArray): Iterable<T>
}

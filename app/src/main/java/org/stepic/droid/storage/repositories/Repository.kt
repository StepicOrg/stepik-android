package org.stepic.droid.storage.repositories

import android.support.annotation.WorkerThread

/**
 * T – type,
 * K – type of Key
 */
interface Repository<T, K> {

    @WorkerThread
    fun getObject(key: K): T?

    @WorkerThread
    fun getObjects(keys: Array<K>): Iterable<T>
}

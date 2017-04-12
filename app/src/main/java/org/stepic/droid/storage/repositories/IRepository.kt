package org.stepic.droid.storage.repositories

import android.support.annotation.WorkerThread

/**
 * T – type,
 * K – type of Key
 */
interface IRepository<T, K> {

    @WorkerThread
    fun getObject(key: K): T?
}

package org.stepic.droid.persistence.adapter

import io.reactivex.Completable

interface DonwloadTaskAdapter {
    fun addTask(vararg ids: Long): Completable
    fun removeTask(id: Long): Completable
}
package org.stepic.droid.core

import android.support.annotation.WorkerThread

interface VideoLengthResolver {
    @WorkerThread
    fun determineLengthInMillis(path: String?): Long?
}

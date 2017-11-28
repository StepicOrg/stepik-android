package org.stepic.droid.core

import android.support.annotation.WorkerThread

interface StepikDevicePoster {
    @WorkerThread
    fun registerDevice()
}

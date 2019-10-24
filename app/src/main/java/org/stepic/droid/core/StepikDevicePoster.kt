package org.stepic.droid.core

import androidx.annotation.WorkerThread

interface StepikDevicePoster {
    @WorkerThread
    fun registerDevice()
}

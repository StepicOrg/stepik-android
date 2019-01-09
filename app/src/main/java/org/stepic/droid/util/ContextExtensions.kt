package org.stepic.droid.util

import android.app.ActivityManager
import android.content.Context

val Context.isMainProcess: Boolean
    get() {
        val pid = android.os.Process.myPid()
        return (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.runningAppProcesses
            ?.find { it.pid == pid }
            ?.processName
            ?.let { it.isNotEmpty() && it == packageName }
            ?: false
    }
package org.stepic.droid.util

import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

fun Fragment.checkSelfPermissions(permissions: List<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this.requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            return false
        }
    return true
}

fun Fragment.requestMultiplePermissions(permissions: List<String>, requestCode: Int) {
    val remainingPermissions = arrayListOf<String>()
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this.requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            remainingPermissions.add(permission)
    }
    this.requestPermissions(remainingPermissions.toTypedArray(), requestCode)
}
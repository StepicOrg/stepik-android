package org.stepic.droid.util

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

fun List<String>.arePermissionsGranted(context: Context): Boolean {
    for (permission in this) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

fun List<String>.requestMultiplePermissions(fragment: Fragment, requestCode: Int) {
    val remainingPermissions = arrayListOf<String>()
    for (permission in this)
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            remainingPermissions.add(permission)
    fragment.requestPermissions(this.toTypedArray(), requestCode)
}
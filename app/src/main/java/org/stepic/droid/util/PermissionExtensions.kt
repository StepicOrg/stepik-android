package org.stepic.droid.util

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

fun Context.checkSelfPermissions(permissions: List<String>): Boolean =
    permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

fun Fragment.requestMultiplePermissions(permissions: List<String>, requestCode: Int) {
    val remainingPermissions = permissions
        .filter{ ContextCompat.checkSelfPermission(this.requireContext(), it) != PackageManager.PERMISSION_GRANTED }
    this.requestPermissions(remainingPermissions.toTypedArray(), requestCode)
}
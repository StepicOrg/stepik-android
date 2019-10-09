package org.stepic.droid.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

fun Context.checkSelfPermissions(permissions: List<String>): Boolean =
    permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

fun Fragment.requestMultiplePermissions(permissions: List<String>, requestCode: Int) {
    val remainingPermissions = permissions
        .filter{ ContextCompat.checkSelfPermission(this.requireContext(), it) != PackageManager.PERMISSION_GRANTED }
    this.requestPermissions(remainingPermissions.toTypedArray(), requestCode)
}
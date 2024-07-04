package org.stepic.droid.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

const val REQUEST_NOTIFICATION_PERMISSION = 3321

fun Context.checkSelfPermissions(permissions: List<String>): Boolean =
    permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

fun Fragment.requestMultiplePermissions(permissions: List<String>, requestCode: Int) {
    val remainingPermissions = permissions
        .filter{ ContextCompat.checkSelfPermission(this.requireContext(), it) != PackageManager.PERMISSION_GRANTED }
    this.requestPermissions(remainingPermissions.toTypedArray(), requestCode)
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.requestMultiplePermissions(permissions: List<String>, requestCode: Int) {
    val remainingPermissions = permissions
        .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
    this.requestPermissions(remainingPermissions.toTypedArray(), requestCode)
}

fun Context.isNotificationPermissionGranted(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
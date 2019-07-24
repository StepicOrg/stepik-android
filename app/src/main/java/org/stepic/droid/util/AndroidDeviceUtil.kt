package org.stepic.droid.util

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager


/** hasCombBar test if device has Combined Bar : only for tablet with Honeycomb or ICS */
fun hasCombinationBar(context: Context): Boolean {
    return (!isPhone(context)
            && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN))
}

fun isPhone(context: Context): Boolean {
    return (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.phoneType != TelephonyManager.PHONE_TYPE_NONE
}
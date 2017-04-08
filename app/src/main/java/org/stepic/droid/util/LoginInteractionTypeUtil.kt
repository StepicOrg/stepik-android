package org.stepic.droid.util

import android.os.Bundle
import org.stepic.droid.analytic.LoginInteractionType

fun LoginInteractionType.toBundle(): Bundle {
    val bundle = Bundle()
    bundle.putString("LoginInteractionType", this.name)
    return bundle
}
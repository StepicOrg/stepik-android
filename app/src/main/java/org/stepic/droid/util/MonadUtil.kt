package org.stepic.droid.util


inline fun <L, R, Z> L?.liftM2(r: R?, block: (L, R) -> Z) =
    this?.let { l -> r?.let { r -> block(l, r) } }
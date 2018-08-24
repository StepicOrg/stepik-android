package org.stepic.droid.util


inline fun <L, R, Z> L?.liftM2(r: R?, block: (L, R) -> Z) =
    this?.let { l -> r?.let { r -> block(l, r) } }

inline infix fun <X, Y, Z> ((X) -> Y).then(crossinline f: (Y) -> Z): (X) -> Z =
        { f(this(it)) }

inline infix fun <X, Y, Z> ((Y) -> Z).compose(crossinline f: (X) -> Y): (X) -> Z =
        { this(f(it)) }
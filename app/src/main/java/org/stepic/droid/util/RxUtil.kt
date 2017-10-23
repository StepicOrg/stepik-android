package org.stepic.droid.util

import io.reactivex.Observable

enum class RxEmpty { INSTANCE }

@Suppress("UNCHECKED_CAST")
fun <T> Observable<T?>.filterNotNull(): Observable<T> =
        this.filter { it != null } as Observable<T>
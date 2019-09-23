package org.stepik.android.domain.progress.mapper

import org.stepik.android.model.Progressable

@JvmName("progressableIterable_getProgresses")
fun Iterable<Progressable>.getProgresses(): Array<String> =
    mapNotNull(Progressable::progress)
        .toTypedArray()
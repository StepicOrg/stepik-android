package org.stepik.android.domain.progress.mapper

import org.stepik.android.model.Progressable

@JvmName("progressableIterable_getProgresses")
fun Iterable<Progressable>.getProgresses(): List<String> =
    mapNotNull(Progressable::progress)
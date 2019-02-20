package org.stepik.android.remote.base.mapper

import org.stepic.droid.util.PagedList
import org.stepik.android.remote.base.model.MetaResponse

inline fun <E, R : MetaResponse> R.toPagedList(selector: (R) -> List<E>): PagedList<E> =
    PagedList(selector(this), page = meta.page, hasNext = meta.hasNext, hasPrev = meta.hasPrevious)
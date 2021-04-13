package org.stepik.android.remote.base.mapper

import ru.nobird.android.core.model.PagedList
import org.stepik.android.remote.base.model.MetaResponse

inline fun <E, R : MetaResponse> R.toPagedList(selector: (R) -> List<E>): PagedList<E> =
    PagedList(selector(this), page = meta.page, hasNext = meta.hasNext, hasPrev = meta.hasPrevious)
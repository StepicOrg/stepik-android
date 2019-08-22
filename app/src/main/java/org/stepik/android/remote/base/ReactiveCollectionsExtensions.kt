package org.stepik.android.remote.base

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.concatWithPagedList
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.base.model.MetaResponse

const val CHUNK_SIZE = 100

inline fun <R> LongArray.chunkedSingleMap(chuckSize: Int = CHUNK_SIZE, mapper: (LongArray) -> Single<List<R>>): Single<List<R>> =
    asIterable()
        .chunked(chuckSize)
        .map { mapper(it.toLongArray()) }
        .let { Single.concat(it) }
        .reduce(emptyList()) { a, b -> a + b }

inline fun <reified T, R> Array<out T>.chunkedSingleMap(chuckSize: Int = CHUNK_SIZE, mapper: (Array<T>) -> Single<List<R>>): Single<List<R>> =
    asIterable()
        .chunked(chuckSize)
        .map { mapper(it.toTypedArray()) }
        .let { Single.concat(it) }
        .reduce(emptyList()) { a, b -> a + b }

/**
 * Downloads all pages until Meta::hasNext is true starting from [page]
 * and returns concatenated [PagedList] of data from all requests with last page information
 * [page] - current page
 * [sourceFactory] - factory of requests
 * [mapper] - mapper for request result
 *
 * @return concatenated [PagedList] of data from all requests with last page information
 */
fun <T, R : MetaResponse> concatAllPages(page: Int = 1, sourceFactory: (page: Int) -> Single<R>, mapper: (R) -> List<T>): Single<PagedList<T>> =
    sourceFactory(page)
        .flatMap { response ->
            val items = response.toPagedList(mapper)
            if (response.meta.hasNext) {
                concatAllPages(page + 1, sourceFactory, mapper)
                    .map { items.concatWithPagedList(it) }
            } else {
                Single.just(items)
            }
        }
package org.stepik.android.remote.base

import io.reactivex.Single

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
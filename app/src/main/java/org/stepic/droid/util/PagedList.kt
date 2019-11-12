package org.stepic.droid.util

class PagedList<E>(
    list: List<E>,

    val page: Int = 1,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false
) : List<E> by list

fun <E> List<E>.concatWithPagedList(pagedList: PagedList<E>): PagedList<E> =
    PagedList(this + pagedList, page = pagedList.page, hasNext = pagedList.hasNext, hasPrev = pagedList.hasPrev)

inline fun <T, R> PagedList<T>.mapPaged(transform: (T) -> R): PagedList<R> =
    PagedList(map(transform), page = page, hasNext = hasNext, hasPrev = hasPrev)

inline fun <T, R : Any> PagedList<T>.mapNotNullPaged(transform: (T) -> R?): PagedList<R> =
    PagedList(mapNotNull(transform), page = page, hasNext = hasNext, hasPrev = hasPrev)
/**
 * Concatenate two paged lists
 */
operator fun <E> PagedList<E>.plus(pagedList: PagedList<E>): PagedList<E> =
    PagedList(this as List<E> + pagedList, page = pagedList.page, hasNext = pagedList.hasNext, hasPrev = hasPrev)

/**
 * Adds element to the end of paged list and returns it
 */
operator fun <E> PagedList<E>.plus(element: E): PagedList<E> =
    PagedList(this as List<E> + element, page = page, hasNext = hasNext, hasPrev = hasPrev)

/**
 * Returns a list containing all elements not matching the given [predicate].
 */
inline fun <E> PagedList<E>.filterNot(predicate: (E) -> Boolean): PagedList<E> =
    PagedList((this as List<E>).filterNot(predicate), page = page, hasNext = hasNext, hasPrev = hasPrev)
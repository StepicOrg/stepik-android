package org.stepic.droid.util

class PagedList<E>(
    list: List<E>,

    val page: Int = 1,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false
) : List<E> by list

fun <E> List<E>.concatWithPagedList(pagedList: PagedList<E>): PagedList<E> =
    PagedList(this + pagedList, page = pagedList.page, hasNext = pagedList.hasNext, hasPrev = pagedList.hasPrev)

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
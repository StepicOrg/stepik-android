package org.stepic.droid.util

class PagedList<E>(
    list: List<E>,

    val page: Int = 1,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false
) : List<E> by list
package org.stepic.droid.mappers

//dagger 2 cannot find class with "in S"
interface Mapper<S, out D> {
    fun map(item: S): D

    fun map(sourceList: List<S>): List<D> = sourceList.map { map(it) }
}

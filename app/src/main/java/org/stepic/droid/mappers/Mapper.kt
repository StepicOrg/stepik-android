package org.stepic.droid.mappers


/**
 * this class is ok, when we don't need some extra information
 */
interface Mapper<in S, out D> {
    fun S.to(): D

    fun List<S>.to(): List<D> {
        val result = ArrayList<D>()
        this.forEach {
            result.add(it.to())
        }
        return result
    }
}

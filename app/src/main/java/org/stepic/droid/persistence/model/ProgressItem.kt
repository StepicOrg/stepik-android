package org.stepic.droid.persistence.model

sealed class ProgressItem(
        open val id: Long // id of lesson, section, course etc
) {
    class NotCached(override val id: Long): ProgressItem(id)
    class Cached(override val id: Long): ProgressItem(id)
    class Pending(override val id: Long): ProgressItem(id)
    class InProgress(override val id: Long, val progress: Float): ProgressItem(id)
}
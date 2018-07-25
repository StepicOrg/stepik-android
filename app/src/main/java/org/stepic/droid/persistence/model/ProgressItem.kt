package org.stepic.droid.persistence.model

data class ProgressItem(
        val id: Long, // id of lesson, section, course etc
        val state: State,
        val progress: Float // progress if state == PENDING
) {
    enum class State {
        NOT_CACHED, CACHED, PENDING, IN_PROGRESS
    }
}
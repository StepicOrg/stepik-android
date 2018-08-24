package org.stepic.droid.persistence.model

class PersistentState(
        val id: Long,
        val type: Type,
        val state: State
) {
    enum class Type {
        COURSE,
        SECTION,
        UNIT,
        LESSON,
        STEP
    }

    enum class State {
        CACHED, IN_PROGRESS, NOT_CACHED
    }
}
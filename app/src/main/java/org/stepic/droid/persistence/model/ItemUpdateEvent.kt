package org.stepic.droid.persistence.model

class ItemUpdateEvent(
        val id: Long,
        val type: Type
) {
    enum class Type {
        UNIT, SECTION
    }
}
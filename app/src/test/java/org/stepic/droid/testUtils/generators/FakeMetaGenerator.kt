package org.stepic.droid.testUtils.generators

import org.stepic.droid.model.Meta

object FakeMetaGenerator {
    @JvmOverloads
    fun generate(page: Int = 1,
                 hasPrevious: Boolean = false,
                 hasNext: Boolean = false
    ): Meta = Meta(page = page, has_previous = hasPrevious, has_next = hasNext)
}
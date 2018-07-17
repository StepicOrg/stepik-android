package org.stepic.droid.testUtils.generators

import org.stepik.android.model.Meta

object FakeMetaGenerator {
    @JvmOverloads
    fun generate(page: Int = 1,
                 hasPrevious: Boolean = false,
                 hasNext: Boolean = false
    ): Meta = Meta(page = page, hasPrevious = hasPrevious, hasNext = hasNext)
}
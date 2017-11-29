package org.stepic.droid.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CoursesDescriptionContainerTest {

    @Test
    fun descriptionContainerIsParcelable() {
        val container = CoursesDescriptionContainer("123", CollectionDescriptionColors.BLUE)
        container.assertThatObjectParcelable<CoursesDescriptionContainer>()
    }
}

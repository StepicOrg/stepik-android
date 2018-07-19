package org.stepic.droid.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CoursesCarouselInfoTest {

    @Test
    fun infoIsParcelable() {
        val info = CoursesCarouselInfo(CoursesCarouselColorType.Light, "hello", null, longArrayOf(1, 3, 10))
        info.assertThatObjectParcelable<CoursesCarouselInfo>()
    }
}

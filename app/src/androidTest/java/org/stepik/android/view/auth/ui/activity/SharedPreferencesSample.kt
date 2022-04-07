package org.stepik.android.view.auth.ui.activity

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.view.course.ui.activity.CourseActivity

class SharedPreferencesSample : TestCase() {

    private val applicationContext = ApplicationProvider.getApplicationContext<Context>()
    private val preferencesEditor = applicationContext.getSharedPreferences("device_specific", Context.MODE_PRIVATE)

    @get:Rule
    val activityScenario = ActivityScenarioRule<CourseActivity>(CourseActivity.createIntent(applicationContext, 101420L, CourseViewSource.Unknown))

    @Test
    fun testOpenCourseScreen() =
        before {
            preferencesEditor.edit(commit = true) {
                /**
                 *  Value can be - DiscountPurple, DiscountGreen, DiscountTransparent
                 */
                putString("split_test_discount_appearance", "DiscountGreen")
            }
        }.after {
//            preferencesEditor.edit(commit = true) { clear() }
        }.run {
            step("Open Course Screen with ID = 101420") {
                Thread.sleep(5000)
            }
        }
}
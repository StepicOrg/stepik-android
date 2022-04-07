package org.stepik.android.view.auth.ui.activity

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SocialAuthScreenTest : TestCase() {
    @get:Rule
    val activityTestRule = ActivityScenarioRule(SocialAuthActivity::class.java)

    @Test
    // @Link("https://vyahhi.myjetbrains.com/youtrack/issue/TESTAPPS-174")
    fun testGuestCanSeeSocialAuthScreen() =
        run {
            step("Should be Social Auth Screen") {
                testLogger.i("Should be Social Auth Screen")
                SocialAuthScreen {
                    shouldBeCredentialAuthScreen()
                }
            }
        }

    @Test
    // @Link("https://vyahhi.myjetbrains.com/youtrack/issue/TESTAPPS-175")
    fun testGuestCanExpandSocialAccounts() =
        run {
            step("Click 'More' button") {
                testLogger.i("Click more button")
                SocialAuthScreen {
                    moreButton {
                        isVisible()
                        isClickable()
                        hasText("More")
                        click()
                    }
                }
            }

            step ("Should not be More button") {
                testLogger.i("After click on More Button it disappear")
                SocialAuthScreen {
                    moreButton {
                        isNotDisplayed()
                    }
                }
            }

            step("Should be Less button") {
                testLogger.i("Should be Less button")
                SocialAuthScreen {
                    lessButton {
                        isVisible()
                        isClickable()
                        hasText("Less")
                    }
                }
            }
        }
}
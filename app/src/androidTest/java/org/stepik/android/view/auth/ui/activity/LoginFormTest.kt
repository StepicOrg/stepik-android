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
            step("Should be dismiss button") {
                testLogger.d("Should be dismiss button on social auth screen")
                SocialAuthScreen {
                    dismissButton {
                        isVisible()
                    }
                }
            }

            step("Should be Stepik logo") {
                testLogger.d("Should be Stepik logo on social auth screen")
                SocialAuthScreen {
                    stepikLogo {
                        isVisible()
                    }
                }
            }

            step("Should be title screen") {
                testLogger.d("Should be title on social auth screen")
                SocialAuthScreen {
                    titleScreen {
                        isVisible()
                        hasText("Sign In with social accounts")
                    }
                }
            }

            step("Should be More button") {
                testLogger.d("Should be more button on social auth screen")
                SocialAuthScreen {
                    moreButton {
                        isVisible()
                        hasText("More")
                    }
                }
            }

            step("Should be sign in with e-mail button") {
                testLogger.d("+")
                SocialAuthScreen {
                    signInWithEmailButton {
                        isVisible()
                        isClickable()
                        hasText("Sign in with e-mail")
                    }
                }
            }

            step("Should be launch sign up button") {
                testLogger.d("---")
                SocialAuthScreen {
                    launchSignUpButton {
                        isVisible()
                        hasText("Sign up")
                    }
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
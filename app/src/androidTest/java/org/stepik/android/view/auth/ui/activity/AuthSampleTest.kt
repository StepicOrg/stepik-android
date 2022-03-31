package org.stepik.android.view.auth.ui.activity

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class AuthSampleTest : TestCase() {
    @get:Rule
    val activityTestRule = ActivityScenarioRule(SocialAuthActivity::class.java)

    @Test
    fun test() =
        run {
            step("Open Social Auth Screen") {
                testLogger.d("I am testLogger")
                SocialAuthScreen {
                    signInWithEmailButton {
                        isVisible()
                        click()
                    }
                }
            }

            step("Open Credential Auth Screen") {
                CredentialAuthScreen {
                    loginField.isVisible()
                    loginField.typeText("test@stepik.org")
                    passwordField.typeText("stepikthebest")
                    loginField.hasText("test@stepik.org")
                }
            }
        }
}
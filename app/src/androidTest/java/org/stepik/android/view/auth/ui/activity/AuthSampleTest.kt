package org.stepik.android.view.auth.ui.activity

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class AuthSampleTest : TestCase() {
    @get:Rule
    val activityTestRule = ActivityScenarioRule(SocialAuthActivity::class.java)

    @Test
    fun testGuestCanLoginWithCorrectCredentials() =
        run {
            step("Open Credential Auth Screen") {
                testLogger.d("Open Credential Auth Screen")
                SocialAuthScreen {
                    openCredentialAuthScreen()
                }
            }

            step("Login with email and password on credential auth screen") {
                testLogger.i("Login with email and password on credential auth screen")
                CredentialAuthScreen {
                    loginWithEmailAndPassword(email = "testlearner@stepik.org", password = "512")
                }
            }

            step("Should be a home screen after login") {
                testLogger.i("Should be a home screen after login")
                MainFeedActivity {
                    shouldBeHomeScreen()
                }
            }
        }
}
package org.stepik.android.view.auth.ui.activity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import org.stepic.droid.R

object SocialAuthScreen : KScreen<SocialAuthScreen>() {

    override val layoutId: Int = R.layout.activity_auth_social
    override val viewClass: Class<*> = SocialAuthActivity::class.java

    val signInWithEmailButton = KButton { withId(R.id.signInWithEmail) }
    val dismissButton = KButton { withId(R.id.dismissButton) }
    val stepikLogo = KButton { withId(R.id.stepikLogo) }
    val titleScreen = KButton { withId(R.id.signInText) }
    val moreButton = KButton { withId(R.id.showMore) }
    val lessButton = KButton { withId(R.id.showLess) }
    val signUpButton = KButton { withId(R.id.launchSignUpButton) }

    fun openCredentialAuthScreen() {
        signInWithEmailButton {
            isVisible()
            click()
        }
    }

    fun shouldBeCredentialAuthScreen() {
        val applicationResources = ApplicationProvider.getApplicationContext<Context>().resources

        dismissButton {
            isVisible()
        }

        stepikLogo {
            isVisible()
        }

        titleScreen {
            val expectedText =
                buildString {
                    append(applicationResources.getString(R.string.sign_in))
                    append(applicationResources.getString(R.string.sign_in_with_social_suffix))
                }
            isVisible()
            hasText(expectedText)
        }

        moreButton {
            isVisible()
            hasText(R.string.social_recycler_show_more)
        }

        signInWithEmailButton {
            isVisible()
            isClickable()
            hasText(R.string.sign_in_with_password)
        }

        signUpButton {
            isVisible()
            isClickable()
            hasText(R.string.sign_up)
        }
    }
}
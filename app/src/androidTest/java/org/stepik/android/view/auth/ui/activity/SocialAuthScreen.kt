package org.stepik.android.view.auth.ui.activity

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
    val launchSignUpButton = KButton { withId(R.id.launchSignUpButton) }
}
package org.stepik.android.view.auth.ui.activity

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.text.KButton
import org.stepic.droid.R

object SocialAuthScreen : KScreen<SocialAuthScreen>() {

    override val layoutId: Int = R.layout.activity_auth_social
    override val viewClass: Class<*> = SocialAuthActivity::class.java

    val signInWithEmailButton = KButton { withId(R.id.signInWithEmail) }
}
package org.stepik.android.view.auth.ui.activity

import org.stepic.droid.R
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText

object CredentialAuthScreen : KScreen<CredentialAuthScreen>() {
    override val layoutId: Int = R.layout.activity_auth_credential
    override val viewClass: Class<*> = CredentialAuthActivity::class.java

    val loginField = KEditText { withId(R.id.loginField) }
    val passwordField = KEditText { withId(R.id.passwordField) }

}
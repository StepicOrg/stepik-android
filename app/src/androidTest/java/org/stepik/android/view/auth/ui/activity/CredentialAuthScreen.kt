package org.stepik.android.view.auth.ui.activity

import org.stepic.droid.R
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText

object CredentialAuthScreen : KScreen<CredentialAuthScreen>() {
    override val layoutId: Int = R.layout.activity_auth_credential
    override val viewClass: Class<*> = CredentialAuthActivity::class.java

    val loginField = KEditText { withId(R.id.loginField) }
    val passwordField = KEditText { withId(R.id.passwordField) }
    val signInButton = KEditText { withId(R.id.loginButton)}
    val forgotPasswordButton = KEditText { withId(R.id.forgotPasswordView)}
    val checkableImageButton = KEditText { withId(R.id.text_input_end_icon)}

    fun loginWithEmailAndPassword(email: String, password: String) {
        loginField {
            isVisible()
            click()
            replaceText(email)
        }

        passwordField {
            isVisible()
            click()
            replaceText(password)
        }

        signInButton {
            isVisible()
            click()
        }
    }
}
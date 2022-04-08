package org.stepik.android.view.auth.ui.activity

import org.stepic.droid.R
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText

object MainFeedActivity : KScreen<MainFeedActivity>() {
    override val layoutId: Int = R.layout.activity_main_feed
    override val viewClass: Class<*> = MainFeedActivity::class.java

    val toolbarTitle = KEditText { withId(R.id.centeredToolbarTitle) }

    fun shouldBeHomeScreen() {
        toolbarTitle {
            isVisible()
            hasText(R.string.home_title)
        }
    }
}
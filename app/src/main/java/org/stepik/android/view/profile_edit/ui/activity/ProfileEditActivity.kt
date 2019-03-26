package org.stepik.android.view.profile_edit.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_profile_edit.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.user.Profile
import org.stepik.android.view.profile_edit.model.ProfileEditItem
import org.stepik.android.view.profile_edit.ui.adapter.ProfileEditAdapter
import javax.inject.Inject

class ProfileEditActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_PROFILE = "profile"

        fun createIntent(context: Context, profile: Profile): Intent =
            Intent(context, ProfileEditActivity::class.java)
                .putExtra(EXTRA_PROFILE, profile)
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    private val profile by lazy { intent.getParcelableExtra<Profile>(EXTRA_PROFILE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        injectComponent()

        initCenteredToolbar(R.string.profile_title, showHomeButton = true, homeIndicator = R.drawable.ic_close_dark)

        val navigationItems = listOf(
            ProfileEditItem(ProfileEditItem.Type.PERSONAL_INFO, getString(R.string.profile_edit_info_title), getString(R.string.profile_edit_info_subtitle)),
            ProfileEditItem(ProfileEditItem.Type.PASSWORD, getString(R.string.profile_edit_password_title), getString(R.string.profile_edit_password_subtitle))
        )

        navigationRecycler.layoutManager = LinearLayoutManager(this)
        navigationRecycler.adapter = ProfileEditAdapter(navigationItems) { item ->
            when (item.type) {
                ProfileEditItem.Type.PERSONAL_INFO ->
                    screenManager.showProfileEditInfo(this, profile)
                ProfileEditItem.Type.PASSWORD ->
                    screenManager.showProfileEditPassword(this, profile.id)
            }
        }

        navigationRecycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            ContextCompat.getDrawable(this@ProfileEditActivity, R.drawable.list_divider_h)?.let(::setDrawable)
        })
    }

    private fun injectComponent() {
        App.component()
            .profileEditComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            false
        }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}
package org.stepik.android.view.profile_edit.ui.activity

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import org.stepic.droid.util.setTextColor
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.profile_edit.ProfileEditPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditView
import org.stepik.android.view.profile_edit.model.ProfileEditItem
import org.stepik.android.view.profile_edit.ui.adapter.ProfileEditAdapter
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class ProfileEditActivity : AppCompatActivity(), ProfileEditView {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, ProfileEditActivity::class.java)
    }

    private lateinit var profileEditPresenter: ProfileEditPresenter

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var profile: Profile? = null
    private lateinit var navigationItems: MutableList<ProfileEditItem>
    private val viewStateDelegate =
        ViewStateDelegate<ProfileEditView.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        injectComponent()
        profileEditPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileEditPresenter::class.java)
        initCenteredToolbar(R.string.profile_title, showHomeButton = true, homeIndicator = R.drawable.ic_close_dark)

        navigationItems = mutableListOf(
            ProfileEditItem(ProfileEditItem.Type.PERSONAL_INFO, getString(R.string.profile_edit_info_title), getString(R.string.profile_edit_info_subtitle)),
            ProfileEditItem(ProfileEditItem.Type.PASSWORD, getString(R.string.profile_edit_password_title), getString(R.string.profile_edit_password_subtitle))
        )

        navigationRecycler.layoutManager = LinearLayoutManager(this)
        navigationRecycler.adapter = ProfileEditAdapter(navigationItems) { item ->
            val profile = profile ?: return@ProfileEditAdapter
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

        viewStateDelegate.addState<ProfileEditView.State.Idle>()
        viewStateDelegate.addState<ProfileEditView.State.Loading>()
        viewStateDelegate.addState<ProfileEditView.State.Error>(profileEditEmptyLogin)
        viewStateDelegate.addState<ProfileEditView.State.ProfileLoaded>(navigationRecycler)
    }

    private fun injectComponent() {
        App.component()
            .profileEditComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileEditPresenter.attachView(this)
    }

    override fun onStop() {
        profileEditPresenter.detachView(this)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            false
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ProfileEditInfoActivity.REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    Snackbar
                        .make(root, R.string.profile_edit_change_success_info, Snackbar.LENGTH_SHORT)
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .show()
                }

            ProfileEditPasswordActivity.REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    Snackbar
                        .make(root, R.string.profile_edit_change_success_password, Snackbar.LENGTH_SHORT)
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .show()
                }

            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun setState(state: ProfileEditView.State) {
        viewStateDelegate.switchState(state)
        if (state is ProfileEditView.State.ProfileLoaded) {
            profile = state.profileWrapper.profile
            state.profileWrapper.primaryEmailAdress?.email?.let {
                navigationItems.add(1, ProfileEditItem(ProfileEditItem.Type.EMAIL, getString(R.string.email), it))
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}
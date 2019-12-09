package org.stepik.android.view.profile.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.header_profile.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.ShareHelper
import org.stepic.droid.ui.activities.contracts.CloseButtonInToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.commitNow
import org.stepik.android.model.user.User
import org.stepik.android.presentation.profile.ProfilePresenter
import org.stepik.android.presentation.profile.ProfileView
import org.stepik.android.view.injection.profile.ProfileComponent
import org.stepik.android.view.profile.ui.activity.ProfileActivity
import org.stepik.android.view.profile.ui.animation.ProfileHeaderAnimationDelegate
import org.stepik.android.view.profile.ui.delegate.ProfileStatsDelegate
import org.stepik.android.view.profile_achievements.ui.fragment.ProfileAchievementsFragment
import org.stepik.android.view.profile_activities.ui.fragment.ProfileActivitiesFragment
import org.stepik.android.view.profile_detail.ui.fragment.ProfileDetailFragment
import org.stepik.android.view.profile_id.ui.fragment.ProfileIdFragment
import org.stepik.android.view.profile_links.ui.fragment.ProfileLinksFragment
import org.stepik.android.view.profile_notification.ui.fragment.ProfileNotificationFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class ProfileFragment : Fragment(), ProfileView {
    companion object {
        fun newInstance(): Fragment =
            newInstance(0)

        fun newInstance(userId: Long): Fragment =
            ProfileFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var shareHelper: ShareHelper

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId by argument<Long>()

    private lateinit var profileComponent: ProfileComponent
    private lateinit var profilePresenter: ProfilePresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<ProfileView.State>

    private lateinit var profileStatsDelegate: ProfileStatsDelegate
    private lateinit var headerAnimationDelegate: ProfileHeaderAnimationDelegate

    private var shareMenuItem: MenuItem? = null
    private var isShareMenuItemVisible: Boolean = false
        set(value) {
            field = value
            shareMenuItem?.isVisible = value
        }

    private var editMenuItem: MenuItem? = null
    private var isEditMenuItemVisible: Boolean = false
        set(value) {
            field = value
            editMenuItem?.isVisible = value
        }

    private var menuTintStateList: ColorStateList = ColorStateList.valueOf(0x0)
        set(value) {
            field = value

            toolbar?.navigationIcon?.let { DrawableCompat.setTintList(it, value) }
            editMenuItem?.let { MenuItemCompat.setIconTintList(it, value) }
            shareMenuItem?.let { MenuItemCompat.setIconTintList(it, value) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        injectComponent()

        profilePresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfilePresenter::class.java)
        profilePresenter.onData(userId)
    }

    private fun injectComponent() {
        profileComponent = App
            .componentManager()
            .profileComponent(userId)
        profileComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ProfileView.State.Idle>()
        viewStateDelegate.addState<ProfileView.State.Loading>(profileLoading)
        viewStateDelegate.addState<ProfileView.State.Content>(scrollContainer)
        viewStateDelegate.addState<ProfileView.State.Empty>(profileEmpty)
        viewStateDelegate.addState<ProfileView.State.EmptyLogin>(profileEmptyLogin)
        viewStateDelegate.addState<ProfileView.State.NetworkError>(profileNetworkError)

        (activity as? AppCompatActivity)
            ?.apply { setSupportActionBar(toolbar) }
            ?.supportActionBar
            ?.apply {
                setDisplayHomeAsUpEnabled(activity is CloseButtonInToolbar)
                setDisplayShowTitleEnabled(false)
            }

        profileStatsDelegate = ProfileStatsDelegate(view)

        ViewCompat.setElevation(header, resources.getDimension(R.dimen.profile_header_elevation))
        toolbar.navigationIcon?.let { DrawableCompat.setTintList(it, menuTintStateList) }

        headerAnimationDelegate =
            ProfileHeaderAnimationDelegate(
                view,
                colorStart = ContextCompat.getColor(requireContext(), R.color.white),
                colorEnd = ContextCompat.getColor(requireContext(), R.color.new_accent_color)
            ) { menuTintStateList = it }

        scrollContainer
            .setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
                headerAnimationDelegate.onScroll(scrollY)
            }
        view.doOnNextLayout { headerAnimationDelegate.onScroll(scrollContainer.scrollY) }

        tryAgain.setOnClickListener { profilePresenter.onData(userId, forceUpdate = true) }
        authAction.setOnClickListener { screenManager.showLaunchScreen(context) }

        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                add(R.id.container, ProfileActivitiesFragment.newInstance(userId))
                add(R.id.container, ProfileAchievementsFragment.newInstance(userId))
                add(R.id.container, ProfileNotificationFragment.newInstance(userId))
                add(R.id.container, ProfileLinksFragment.newInstance(userId))
                add(R.id.container, ProfileDetailFragment.newInstance(userId))
                add(R.id.container, ProfileIdFragment.newInstance(userId))
            }
        }

        if (activity !is ProfileActivity) {
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)

        editMenuItem = menu.findItem(R.id.menu_item_edit)
        editMenuItem?.isVisible = isEditMenuItemVisible
        editMenuItem?.let { MenuItemCompat.setIconTintList(it, menuTintStateList) }

        shareMenuItem = menu.findItem(R.id.menu_item_share)
        shareMenuItem?.isVisible = isShareMenuItemVisible
        shareMenuItem?.let { MenuItemCompat.setIconTintList(it, menuTintStateList) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_edit -> {
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.ProfileEdit.SCREEN_OPENED)
                screenManager.showProfileEdit(context)
                true
            }

            R.id.menu_item_share -> {
                profilePresenter.onShareProfileClicked()
                true
            }

            R.id.menu_item_settings -> {
                analytic.reportEvent(Analytic.Screens.USER_OPEN_SETTINGS)
                screenManager.showSettings(activity)
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()
        profilePresenter.attachView(this)
    }

    override fun onStop() {
        profilePresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: ProfileView.State) {
        viewStateDelegate.switchState(state)

        when (state) {
            is ProfileView.State.Content -> {
                with(state.profileData) {
                    Glide
                        .with(this@ProfileFragment)
                        .load(user.avatar)
                        .placeholder(R.drawable.general_placeholder)
                        .into(profileImage)

                    profileName.text = user.fullName
                    profileBio.text = user.shortBio
                    profileBio.isVisible = !user.shortBio.isNullOrBlank()

                    toolbarTitle.text = user.fullName
                    toolbarTitle.translationY = 1000f

                    isEditMenuItemVisible = isCurrentUser
                    isShareMenuItemVisible = true

                    profileStatsDelegate.setProfileStats(user)

                    profileCover.isVisible = !user.cover.isNullOrEmpty()
                    Glide
                        .with(requireContext())
                        .asBitmap()
                        .centerCrop()
                        .load(user.cover)
                        .into(profileCover)

                    view?.doOnNextLayout { headerAnimationDelegate.onScroll(scrollContainer.scrollY) }
                    sendScreenOpenEvent(isCurrentUser)
                }
            }

            else -> {
                toolbarTitle.setText(R.string.profile_title)
                toolbarTitle.translationY = 0f

                isEditMenuItemVisible = false
                isShareMenuItemVisible = false
            }
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun shareUser(user: User) {
        startActivity(shareHelper.getIntentForUserSharing(user))
    }

    private fun sendScreenOpenEvent(isCurrentUser: Boolean) {
        val state = if (isCurrentUser) {
            getString(R.string.profile_self_state)
        } else {
            getString(R.string.profile_other_state)
        }
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Profile.PROFILE_SCREEN_OPENED, mapOf(
            AmplitudeAnalytic.Profile.Params.STATE to state
        ))
        analytic.reportEvent(Analytic.Profile.PROFILE_SCREEN_OPENED, Bundle().apply {
            putString(Analytic.Profile.Params.STATE, state)
        })
    }
}
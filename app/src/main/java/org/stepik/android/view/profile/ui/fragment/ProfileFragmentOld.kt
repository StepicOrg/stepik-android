package org.stepik.android.view.profile.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.view.*
import kotlinx.android.synthetic.main.fragment_profile_old.*
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import kotlinx.android.synthetic.main.view_notification_interval_chooser.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.ShareHelper
import org.stepic.droid.features.achievements.ui.adapters.AchievementsTileAdapter
import org.stepic.droid.features.achievements.ui.adapters.BaseAchievementsAdapter
import org.stepic.droid.features.achievements.ui.dialogs.AchievementDetailsDialog
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.activities.contracts.CloseButtonInToolbar
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.util.CloseIconHolder.getCloseIconDrawableRes
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.ProfileSettingsHelper
import org.stepic.droid.util.copyTextToClipboard
import org.stepic.droid.util.glide.GlideSvgRequestFactory
import org.stepic.droid.viewmodel.ProfileSettingsViewModel
import org.stepik.android.presentation.profile_old.ProfilePresenter
import org.stepik.android.presentation.profile_old.ProfileView
import org.stepik.android.view.profile.ui.adapter.ProfileSettingsAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class ProfileFragmentOld : Fragment(), ProfileView, TimeIntervalPickerDialogFragment.Companion.Callback {

    companion object {
        private const val MAX_ACHIEVEMENTS_TO_DISPLAY = 6
        private const val DETAILED_INFO_CONTAINER_KEY = "detailedInfoContainerKey"

        fun newInstance(): ProfileFragmentOld =
            newInstance(0)

        fun newInstance(userId: Long = 0): ProfileFragmentOld =
            ProfileFragmentOld().apply {
                this.userId = userId
            }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var shareHelper: ShareHelper

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var profilePresenter: ProfilePresenter

    private var profileSettingsAdapter: DefaultDelegateAdapter<ProfileSettingsViewModel> = DefaultDelegateAdapter()

    private var achievementsToDisplay: Int = 0
    private var isShortInfoExpanded: Boolean = false
    private var userId: Long by argument()
    private var localUserViewModel: UserViewModel? = null

    private val viewStateDelegate =
        ViewStateDelegate<ProfileView.State>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        injectComponent()
        analytic.reportEvent(Analytic.Profile.OPEN_SCREEN_OVERALL)

        profilePresenter = ViewModelProviders.of(this, viewModelFactory).get(ProfilePresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .profileComponentBuilderNew()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_old, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        achievementsToDisplay = resources.getInteger(R.integer.achievements_to_display)
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTimezone()

        // app:fontFamily doesn't work on this view
        notificationStreakSwitch.typeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_light)

        // Profile recycler
        setupProfileSettingsAdapter()
        with(profileSettingsRecyclerView) {
            adapter = profileSettingsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        achievementsTilesContainer.layoutManager = GridLayoutManager(context, achievementsToDisplay)
        achievementsTilesContainer.adapter = AchievementsTileAdapter().apply {
            onAchievementItemClick = {
                AchievementDetailsDialog
                    .newInstance(it, localUserViewModel?.isMyProfile ?: false)
                    .show(childFragmentManager, AchievementDetailsDialog.TAG)
            }
        }
        achievementsTilesContainer.isNestedScrollingEnabled = false
        initAchievementsPlaceholders()

        profileImage.setOnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_IMAGE) }
        val clickStreakValue = View.OnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_STREAK_VALUE) }
        currentStreakValue.setOnClickListener(clickStreakValue)
        maxStreakValue.setOnClickListener(clickStreakValue)
        profileName.setOnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_FULL_NAME) }

        notificationIntervalChooserContainer.setOnClickListener {
            val supportFragmentManager = activity
                ?.supportFragmentManager
                ?: return@setOnClickListener

            val dialog = TimeIntervalPickerDialogFragment.newInstance()
            dialog.setTargetFragment(this@ProfileFragmentOld, 0)
            dialog.showIfNotExists(supportFragmentManager, TimeIntervalPickerDialogFragment.TAG)
            analytic.reportEvent(Analytic.Interaction.CLICK_CHOOSE_NOTIFICATION_INTERVAL)
        }

        shortBioInfoContainer.setOnClickListener {
            changeStateOfUserInfo()
        }

        authAction.setOnClickListener {
            screenManager.showLaunchScreen(context, true, MainFeedActivity.PROFILE_INDEX)
        }

        shortBioSecondText.textView.textSize = 14f
        shortBioSecondText.textView.setLineSpacing(0f, 1.6f)

        achievementsLoadingError.tryAgain.setOnClickListener {
            profilePresenter.showAchievementsForUser(localUserViewModel?.id ?: 0, MAX_ACHIEVEMENTS_TO_DISPLAY, true)
        }
        viewAllAchievements.setOnClickListener {
            screenManager.showAchievementsList(context, localUserViewModel?.id ?: 0, localUserViewModel?.isMyProfile ?: false)
        }
        certificatesTitleContainer.setOnClickListener {
            screenManager.showCertificates(requireContext(), userId)
        }

        initViewStateDelegate()
        profilePresenter.initProfile(userId)
    }

    override fun onStart() {
        super.onStart()
        profilePresenter.attachView(this)
    }

    override fun onStop() {
        profilePresenter.detachView(this)
        super.onStop()
    }

    private fun changeStateOfUserInfo() {
        shortBioArrowImageView.changeState()
        val isExpanded = shortBioArrowImageView.isExpanded()
        isShortInfoExpanded = isExpanded
        if (isExpanded) {
            StepikAnimUtils.expand(detailedInfoContainer)
        } else {
            StepikAnimUtils.collapse(detailedInfoContainer)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(DETAILED_INFO_CONTAINER_KEY, isShortInfoExpanded)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            isShortInfoExpanded = it.getBoolean(DETAILED_INFO_CONTAINER_KEY)
            restoreVisibility(detailedInfoContainer, it, DETAILED_INFO_CONTAINER_KEY)
        }
    }

    private fun restoreVisibility(view: View, bundle: Bundle, bundleKey: String) {
        view.visibility = if (bundle.getBoolean(bundleKey)) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<ProfileView.State.Loading>(profileLoadingView)
        viewStateDelegate.addState<ProfileView.State.NetworkError>(profileReportProblem)
        viewStateDelegate.addState<ProfileView.State.UserNotFoundError>(profileEmptyUser)
        viewStateDelegate.addState<ProfileView.State.NeedAuthError>(profileNeedAuth)
    }

    override fun setState(state: ProfileView.State) {
        if (state is ProfileView.State.ProfileLoaded) {
            /***
             *  Too many views inside contentRoot
             */
            profileNeedAuth.visibility = View.GONE
            profileEmptyUser.visibility = View.GONE
            profileReportProblem.visibility = View.GONE
            profileLoadingView.visibility = View.GONE
            contentRoot.visibility = View.VISIBLE

            val userViewModel = state.userLocalViewModel
            this.localUserViewModel = userViewModel
            activity?.invalidateOptionsMenu()
            if (userViewModel.isMyProfile) {
                profilePresenter.showNotificationSetting()
                profileSettingsRecyclerView.visibility = View.VISIBLE

                notificationIntervalChooserContainer.visibility = View.VISIBLE
                setupUserId()
            } else {
                // show user info expanded for strangers
                if (!shortBioArrowImageView.isExpanded()) {
                    changeStateOfUserInfo()
                }
            }

            if (!userViewModel.isPrivate && !userViewModel.isOrganization) {
                profilePresenter.showAchievementsForUser(
                    userViewModel.id,
                    MAX_ACHIEVEMENTS_TO_DISPLAY
                )
                certificatesTitleContainer.visibility = View.VISIBLE
            }

            mainInfoRoot.visibility = View.VISIBLE
            val nameArray = userViewModel
                .fullName
                .split("\\s+".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val builder = StringBuilder()
            for (nameArrayItem in nameArray) {
                if (builder.isNotEmpty()) {
                    builder.append("\n")
                }
                builder.append(nameArrayItem)
            }

            profileName.text = builder.toString()
            val userPlaceholder =
                ContextCompat.getDrawable(requireContext(), R.drawable.general_placeholder)
            if (userViewModel.imageLink != null && userViewModel.imageLink.endsWith(AppConstants.SVG_EXTENSION)) {
                val svgRequestBuilder = GlideSvgRequestFactory.create(context, userPlaceholder)
                val uri = Uri.parse(userViewModel.imageLink)
                svgRequestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .load(uri)
                    .into(profileImage)
            } else {
                Glide.with(requireContext())
                    .asBitmap()
                    .load(userViewModel.imageLink)
                    .placeholder(userPlaceholder)
                    .into(profileImage)
            }

            with(userViewModel) {
                shortBioInfoContainer.isVisible = shortBio.isNotBlank() || information.isNotBlank()
                shortBioSecondHeader.isVisible = shortBio.isNotBlank() && information.isNotBlank()
                when {
                    shortBio.isBlank() && information.isNotBlank() ->
                        shortBioFirstHeader.setText(R.string.user_info) // show header with 'information'

                    shortBio.isNotBlank() && information.isBlank() ->
                        shortBioFirstHeader.setText(R.string.short_bio)

                    shortBio.isNotBlank() && information.isNotBlank() -> { // show general header and all info
                        shortBioFirstHeader.setText(R.string.short_bio_and_info)
                        shortBioSecondHeader.setText(R.string.user_info)
                    }
                }

                if (shortBio.isBlank()) {
                    shortBioFirstText.visibility = View.GONE
                } else {
                    shortBioFirstText.text = shortBio.trim()
                    shortBioFirstText.visibility = View.VISIBLE
                }

                if (information.isBlank()) {
                    shortBioSecondText.visibility = View.GONE
                } else {
                    shortBioSecondText.setPlainOrLaTeXTextWithCustomFontColored(
                        information, R.font.roboto_light, R.color.new_accent_color, false
                    )
                    shortBioSecondText.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun showAchievements(achievements: List<AchievementFlatItem>) {
        (achievementsTilesContainer.adapter as BaseAchievementsAdapter).achievements = achievements.take(achievementsToDisplay)
        achievementsLoadingPlaceholder.isVisible = false
        achievementsLoadingError.isVisible = false
        achievementsTilesContainer.isVisible = true
        achievementsContainer.isVisible = true
    }

    override fun onAchievementsLoadingError() {
        achievementsContainer.isVisible = true
        achievementsLoadingPlaceholder.isVisible = false
        achievementsLoadingError.isVisible = true
        achievementsTilesContainer.isVisible = false
    }

    override fun onAchievementsLoading() {
        achievementsContainer.isVisible = true
        achievementsLoadingPlaceholder.isVisible = true
        achievementsLoadingError.isVisible = false
        achievementsTilesContainer.isVisible = false
    }

    override fun showNotificationEnabledState(notificationEnabled: Boolean, notificationTimeValue: String) {
        notificationStreakSwitch.isChecked = notificationEnabled
        if (notificationStreakSwitch.visibility != View.VISIBLE) {
            notificationStreakSwitch.visibility = View.VISIBLE
        }
        if (notificationEnabled) {
            hideNotificationTime(false)
        } else {
            hideNotificationTime(true)
        }

        notificationStreakSwitch.setOnCheckedChangeListener { _, isChecked ->
            profilePresenter.switchNotificationStreak(isChecked)
        }

        // need to set for show default value, when user enable it
        notificationIntervalTitle.text = resources.getString(R.string.notification_time, notificationTimeValue)
    }

    override fun hideNotificationTime(needHide: Boolean) {
        if (needHide) {
            StepikAnimUtils.collapse(notificationIntervalChooserContainer)
        } else {
            StepikAnimUtils.expand(notificationIntervalChooserContainer)
        }
    }

    override fun setNewTimeInterval(timePresentationString: String) {
        notificationIntervalTitle.text = resources.getString(R.string.notification_time, timePresentationString)
    }

    override fun onStreaksLoaded(currentStreak: Int, maxStreak: Int, haveSolvedToday: Boolean) {
        val suffixCurrent = resources.getQuantityString(R.plurals.day_number, currentStreak)
        val suffixMax = resources.getQuantityString(R.plurals.day_number, maxStreak)

        currentStreakValue.text = String.format("%d %s", currentStreak, suffixCurrent)
        maxStreakValue.text = String.format("%d %s", maxStreak, suffixMax)

        if (haveSolvedToday) {
            streakIndicator.setImageResource(R.drawable.ic_lightning)
        } else {
            streakIndicator.setImageResource(R.drawable.ic_lightning_inactive)
        }

        setStreakInfoVisibility(true)
    }

    override fun onTimeIntervalPicked(chosenInterval: Int) {
        profilePresenter.setStreakTime(chosenInterval)
        analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_PROFILE, chosenInterval.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (localUserViewModel != null) {
            inflater.inflate(R.menu.profile_menu, menu)

            menu.findItem(R.id.menu_item_edit)?.isVisible =
                localUserViewModel?.isMyProfile == true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_share -> {
                shareProfile()
                true
            }
            R.id.menu_item_edit -> {
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.ProfileEdit.SCREEN_OPENED)
                screenManager.showProfileEdit(context)
                true
            }
            else ->
                false
        }

    private fun shareProfile() {
        localUserViewModel?.let {
            val intent = shareHelper.getIntentForProfileSharing(it)
            startActivity(intent)
        }
    }

    private fun setStreakInfoVisibility(needShow: Boolean) {
        currentStreakSuffix.isInvisible = !needShow
        currentStreakValue.isInvisible = !needShow
        maxStreakSuffix.isInvisible = !needShow
        maxStreakValue.isInvisible = !needShow
        streakIndicator.isInvisible = !needShow
    }

    private fun initTimezone() {
        val timezone = TimeZone.getDefault()
        val nowDate = Date()
        val isDaylight = timezone.inDaylightTime(nowDate)
        val print = String.format("%s\n%s",
            DateTimeHelper.hourMinutesOfMidnightDiffWithUtc(timezone, nowDate),
            timezone.getDisplayName(isDaylight, TimeZone.LONG))
        notificationTimeZoneInfo.text = getString(R.string.streak_updated_timezone, print)
    }

    private fun initToolbar() {
        val needCloseButton = context is CloseButtonInToolbar
        initCenteredToolbar(R.string.profile_title, needCloseButton, getCloseIconDrawableRes())
    }

    private fun initAchievementsPlaceholders() {
        for (i in 0 until achievementsToDisplay) {
            val view = layoutInflater.inflate(R.layout.view_achievement_tile_placeholder, achievementsLoadingPlaceholder, false)
            view.layoutParams = (view.layoutParams as LinearLayout.LayoutParams).apply {
                weight = 1f
                width = 0
            }
            achievementsLoadingPlaceholder.addView(view)
        }
    }

    private fun setupUserId() {
        profileIdSeparator.visibility = View.VISIBLE
        profileId.visibility = View.VISIBLE
        profileId.text = getString(R.string.profile_user_id, localUserViewModel?.id)
        profileId.setOnLongClickListener {
            val textToCopy = (it as TextView).text.toString()
            requireContext().copyTextToClipboard(textToCopy = textToCopy, toastMessage = getString(R.string.copied_to_clipboard_toast))
            true
        }
    }

    private fun setupProfileSettingsAdapter() {
        profileSettingsAdapter.items = ProfileSettingsHelper.getProfileSettings()
        profileSettingsAdapter += ProfileSettingsAdapterDelegate(
            onItemClick = {
                when (it) {
                    R.string.settings_title -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_SETTINGS)
                        screenManager.showSettings(requireActivity())
                    }

                    R.string.downloads -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_DOWNLOADS)
                        screenManager.showDownloads(requireContext())
                    }

                    R.string.feedback_title -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_FEEDBACK)
                        screenManager.openFeedbackActivity(requireActivity())
                    }

                    R.string.about_app_title -> {
                        analytic.reportEvent(Analytic.Screens.USER_OPEN_ABOUT_APP)
                        screenManager.openAboutActivity(requireActivity())
                    }

                    R.string.logout_title -> {
                        val supportFragmentManager = activity
                            ?.supportFragmentManager
                            ?: return@ProfileSettingsAdapterDelegate

                        val dialog = LogoutAreYouSureDialog.newInstance()
                        dialog.showIfNotExists(supportFragmentManager, LogoutAreYouSureDialog.TAG)
                        analytic.reportEvent(Analytic.Screens.USER_LOGOUT)
                    }
                }
            }
        )
    }
}
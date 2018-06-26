package org.stepic.droid.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_profile_new.*
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.view.*
import kotlinx.android.synthetic.main.view_notification_interval_chooser.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.ProfilePresenter
import org.stepic.droid.core.presenters.StreakPresenter
import org.stepic.droid.core.presenters.contracts.NotificationTimeView
import org.stepic.droid.core.presenters.contracts.ProfileView
import org.stepic.droid.features.achievements.presenters.AchievementsPresenter
import org.stepic.droid.features.achievements.presenters.AchievementsView
import org.stepic.droid.features.achievements.ui.adapters.AchievementsTileAdapter
import org.stepic.droid.features.achievements.ui.adapters.BaseAchievementsAdapter
import org.stepic.droid.features.achievements.ui.dialogs.AchievementDetailsDialog
import org.stepic.droid.fonts.FontType
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.ui.activities.contracts.CloseButtonInToolbar
import org.stepic.droid.ui.adapters.ProfileSettingsAdapter
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.ProfileSettingsHelper
import org.stepic.droid.util.argument
import org.stepic.droid.util.svg.GlideSvgRequestFactory
import org.stepic.droid.viewmodel.ProfileSettingsViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ProfileFragment : FragmentBase(),
        ProfileView,
        NotificationTimeView,
        AchievementsView {

    companion object {
        private const val NOTIFICATION_INTERVAL_REQUEST_CODE = 11
        private const val MAX_ACHIEVEMENTS_TO_DISPLAY = 6
        private const val DETAILED_INFO_CONTAINER_KEY = "detailedInfoContainerKey"

        fun newInstance(): ProfileFragment = newInstance(0)

        fun newInstance(userId: Long = 0) = ProfileFragment().apply {
            this.userId = userId
        }
    }

    @Inject
    lateinit var profilePresenter: ProfilePresenter

    @Inject
    lateinit var streakPresenter: StreakPresenter

    @Inject
    lateinit var achievementsPresenter: AchievementsPresenter

    private var userId: Long by argument()
    private var localUserViewModel: UserViewModel? = null
    private var profileSettingsList: ArrayList<ProfileSettingsViewModel> = ArrayList()
    private var isShortInfoExpanded: Boolean = false

    private var achievementsToDisplay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytic.reportEvent(Analytic.Profile.OPEN_SCREEN_OVERALL)
        setHasOptionsMenu(true)
        profileSettingsList.clear()
        profileSettingsList.addAll(ProfileSettingsHelper.getProfileSettings())
    }

    override fun injectComponent() {
        App
                .component()
                .profileComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_profile_new, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        achievementsToDisplay =  context.resources.getInteger(R.integer.achievements_to_display)
        nullifyActivityBackground()
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTimezone()

        profileSettingsRecyclerView.layoutManager = LinearLayoutManager(context)
        profileSettingsRecyclerView.adapter = ProfileSettingsAdapter(activity, profileSettingsList, screenManager, this, analytic)
        profileSettingsRecyclerView.isNestedScrollingEnabled = false

        achievementsTilesContainer.layoutManager = GridLayoutManager(context, achievementsToDisplay)
        achievementsTilesContainer.adapter = AchievementsTileAdapter().apply { onAchievementItemClick = {
            AchievementDetailsDialog.newInstance(it, localUserViewModel?.isMyProfile ?: false).show(childFragmentManager, AchievementDetailsDialog.TAG)
        }}
        achievementsTilesContainer.isNestedScrollingEnabled = false
        initAchievementsPlaceholders()

        profilePresenter.attachView(this)
        streakPresenter.attachView(this)
        achievementsPresenter.attachView(this)
        profilePresenter.initProfile(userId)

        profileImage.setOnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_IMAGE) }
        val clickStreakValue = View.OnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_STREAK_VALUE) }
        currentStreakValue.setOnClickListener(clickStreakValue)
        maxStreakValue.setOnClickListener(clickStreakValue)
        profileName.setOnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_FULL_NAME) }

        notificationIntervalChooserContainer.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_CHOOSE_NOTIFICATION_INTERVAL)
            val dialogFragment = TimeIntervalPickerDialogFragment.newInstance()
            if (!dialogFragment.isAdded) {
                dialogFragment.setTargetFragment(this@ProfileFragment, NOTIFICATION_INTERVAL_REQUEST_CODE)
                dialogFragment.show(fragmentManager, null)
            }
        }

        shortBioInfoContainer.setOnClickListener {
            changeStateOfUserInfo()
        }

        authAction.setOnClickListener {
            screenManager.showLaunchScreen(context, true, MainFeedActivity.PROFILE_INDEX)
        }

        shortBioSecondText.textView.textSize = 14f
        shortBioSecondText.textView.setLineSpacing(0f, 1.6f)

        achievementsLoadingError.tryAgain.setOnClickListener { achievementsPresenter.showAchievementsForUser(localUserViewModel?.id ?: 0, MAX_ACHIEVEMENTS_TO_DISPLAY, true) }
        viewAllAchievements.setOnClickListener { screenManager.showAchievementsList(context, localUserViewModel?.id ?: 0, localUserViewModel?.isMyProfile ?: false) }
    }

    override fun onDestroyView() {
        notificationStreakSwitch.setOnCheckedChangeListener(null)
        profileName.setOnClickListener(null)
        currentStreakValue.setOnClickListener(null)
        maxStreakValue.setOnClickListener(null)
        profileImage.setOnClickListener(null)
        notificationIntervalChooserContainer.setOnClickListener(null)
        achievementsPresenter.detachView(this)
        streakPresenter.detachView(this)
        profilePresenter.detachView(this)
        shortBioInfoContainer.setOnClickListener(null)
        isShortInfoExpanded = detailedInfoContainer.visibility == View.VISIBLE
        super.onDestroyView()
        Timber.d("onDestroyView %s", this)
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

    override fun showAchievements(achievements: List<AchievementFlatItem>) {
        (achievementsTilesContainer.adapter as BaseAchievementsAdapter).achievements = achievements.take(achievementsToDisplay)
        achievementsLoadingPlaceholder.changeVisibility(false)
        achievementsLoadingError.changeVisibility(false)
        achievementsTilesContainer.changeVisibility(true)
        achievementsContainer.changeVisibility(true)
    }

    override fun onAchievementsLoadingError() {
        achievementsContainer.changeVisibility(true)
        achievementsLoadingPlaceholder.changeVisibility(false)
        achievementsLoadingError.changeVisibility(true)
        achievementsTilesContainer.changeVisibility(false)
    }

    override fun onAchievementsLoading() {
        achievementsContainer.changeVisibility(true)
        achievementsLoadingPlaceholder.changeVisibility(true)
        achievementsLoadingError.changeVisibility(false)
        achievementsTilesContainer.changeVisibility(false)
    }

    /**
     * This method is invoked only for My Profile
     */
    @SuppressLint("SetTextI18n")
    override fun streaksAreLoaded(currentStreak: Int, maxStreak: Int, haveSolvedToday: Boolean) {
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

    override fun showLoadingAll() {
        contentRoot.visibility = View.GONE
        profileEmptyUser.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileNeedAuth.visibility = View.GONE
        profileLoadingView.visibility = View.VISIBLE
    }

    override fun showNameImageShortBio(userViewModel: UserViewModel) {
        profileNeedAuth.visibility = View.GONE
        profileEmptyUser.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.VISIBLE

        localUserViewModel = userViewModel
        activity.invalidateOptionsMenu()
        if (userViewModel.isMyProfile) {
            streakPresenter.tryShowNotificationSetting()

            //// TODO: 21.08.17 init here and do not spend resources for creating recycler on the another profiles
            profileSettingsRecyclerView.visibility = View.VISIBLE

            notificationIntervalChooserContainer.visibility = View.VISIBLE
        } else {
            //show user info expanded for strangers
            if (!shortBioArrowImageView.isExpanded()) {
                changeStateOfUserInfo()
            }
        }

        if (!userViewModel.isPrivate) {
            achievementsPresenter.showAchievementsForUser(userViewModel.id, MAX_ACHIEVEMENTS_TO_DISPLAY)
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
        val userPlaceholder = ContextCompat.getDrawable(context, R.drawable.general_placeholder)
        if (userViewModel.imageLink != null && userViewModel.imageLink.endsWith(AppConstants.SVG_EXTENSION)) {
            val svgRequestBuilder = GlideSvgRequestFactory.create(context, userPlaceholder)
            val uri = Uri.parse(userViewModel.imageLink)
            svgRequestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .load(uri)
                    .into(profileImage)

        } else {
            Glide.with(context)
                    .load(userViewModel.imageLink)
                    .asBitmap()
                    .placeholder(userPlaceholder)
                    .into(profileImage)
        }

        with(userViewModel) {
            when {
                shortBio.isBlank() && information.isBlank() ->
                    shortBioInfoContainer.visibility = View.GONE //do not show any header

                shortBio.isBlank() && information.isNotBlank() ->
                    shortBioFirstHeader.setText(R.string.user_info) //show header with 'information'

                shortBio.isNotBlank() && information.isBlank() ->
                    shortBioFirstHeader.setText(R.string.short_bio)

                shortBio.isNotBlank() && information.isNotBlank() -> { //show general header and all info
                    shortBioFirstHeader.setText(R.string.short_bio_and_info)
                    shortBioSecondHeader.visibility = View.VISIBLE
                    shortBioSecondHeader.setText(R.string.user_info)
                }
            }

            if (shortBio.isBlank()) {
                shortBioFirstText.visibility = View.GONE
            } else {
                shortBioFirstText.text = shortBio.trim()
            }

            if (information.isBlank()) {
                shortBioSecondText.visibility = View.GONE
            } else {
                shortBioSecondText.setPlainOrLaTeXTextWithCustomFontColored(
                        information, fontsProvider.provideFontPath(FontType.light), R.color.new_accent_color, false)
            }
        }
    }

    override fun onInternetFailed() {
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.GONE
        profileEmptyUser.visibility = View.GONE
        profileNeedAuth.visibility = View.GONE
        profileReportProblem.visibility = View.VISIBLE
    }

    override fun onProfileNotFound() {
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileNeedAuth.visibility = View.GONE
        profileEmptyUser.visibility = View.VISIBLE
    }

    override fun onUserNotAuth() {
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileEmptyUser.visibility = View.GONE
        profileNeedAuth.visibility = View.VISIBLE
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
            streakPresenter.switchNotificationStreak(isChecked)
            hideNotificationTime(!isChecked)
        }

        //need to set for show default value, when user enable it
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTIFICATION_INTERVAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val intervalCode = data!!.getIntExtra(TimeIntervalPickerDialogFragment.RESULT_INTERVAL_CODE_KEY, TimeIntervalUtil.defaultTimeCode)
                streakPresenter.setStreakTime(intervalCode)
                analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_PROFILE, intervalCode.toString() + "")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_CANCELED_PROFILE)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        if (localUserViewModel != null) {
            inflater.inflate(R.menu.share_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_share -> {
                shareProfile()
                return true
            }
        }
        return false
    }

    private fun shareProfile() {
        localUserViewModel?.let {
            val intent = shareHelper.getIntentForProfileSharing(it)
            startActivity(intent)
        }
    }

    private fun setStreakInfoVisibility(needShow: Boolean) {
        val visibility = if (needShow) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        currentStreakSuffix.visibility = visibility
        currentStreakValue.visibility = visibility
        maxStreakSuffix.visibility = visibility
        maxStreakValue.visibility = visibility
        streakIndicator.visibility = visibility
    }

}

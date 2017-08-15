package org.stepic.droid.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_profile_new.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatterBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.ProfilePresenter
import org.stepic.droid.core.presenters.StreakPresenter
import org.stepic.droid.core.presenters.contracts.NotificationTimeView
import org.stepic.droid.core.presenters.contracts.ProfileView
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.ui.adapters.ProfileSettingsAdapter
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProfileSettingsHelper
import org.stepic.droid.util.svg.GlideSvgRequestFactory
import org.stepic.droid.viewmodel.ProfileSettingsViewModel
import timber.log.Timber
import javax.inject.Inject

class ProfileFragment : FragmentBase(),
        ProfileView,
        NotificationTimeView {

    @Inject
    lateinit var profilePresenter: ProfilePresenter

    @Inject
    lateinit var streakPresenter: StreakPresenter

    private var userId: Long = 0
    private var localUserViewModel: UserViewModel? = null
    private var profileSettingsList: ArrayList<ProfileSettingsViewModel> = ArrayList()
    private var isShortInfoExpanded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
        userId = arguments.getLong(USER_ID_KEY)
        analytic.reportEvent(Analytic.Profile.OPEN_SCREEN_OVERALL)
        setHasOptionsMenu(true)
        profileSettingsList.clear()
        profileSettingsList.addAll(ProfileSettingsHelper.getProfileSettings(screenManager))
        Timber.d("onCreate %s", this)
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
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTimezone()

        profileSettingsRecyclerView.layoutManager = LinearLayoutManager(context)
        profileSettingsRecyclerView.adapter = ProfileSettingsAdapter(activity, profileSettingsList, screenManager)
        profileSettingsRecyclerView.isNestedScrollingEnabled = false

        profilePresenter.attachView(this)
        streakPresenter.attachView(this)
        profilePresenter.initProfile(userId)
        profileImage.setOnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_IMAGE) }
        val clickStreakValue = View.OnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_STREAK_VALUE) }
        currentStreakValue.setOnClickListener(clickStreakValue)
        maxStreakValue.setOnClickListener(clickStreakValue)
        profileName.setOnClickListener { analytic.reportEvent(Analytic.Profile.CLICK_FULL_NAME) }

        notificationIntervalChooser.setOnClickListener {
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

        Timber.d("onViewCreated %s", this)
    }

    override fun onDestroyView() {
        notificationStreakSwitch.setOnCheckedChangeListener(null)
        profileName.setOnClickListener(null)
        currentStreakValue.setOnClickListener(null)
        maxStreakValue.setOnClickListener(null)
        profileImage.setOnClickListener(null)
        notificationIntervalChooser.setOnClickListener(null)
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

    private val detailedInfoContainerKey = "detailedInfoContainerKey"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(detailedInfoContainerKey, isShortInfoExpanded)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            isShortInfoExpanded = it.getBoolean(detailedInfoContainerKey)
            restoreVisibility(detailedInfoContainer, it, detailedInfoContainerKey)
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
        val dateTimeFormatter = DateTimeFormatterBuilder()
                .appendHourOfDay(2)
                .appendLiteral(":00")
                .appendLiteral("\n(")
                .appendTimeZoneName()
                .appendLiteral(')')
                .toFormatter()
        val utc = DateTime.now(DateTimeZone.UTC).withMillisOfDay(0)
        val print = dateTimeFormatter.print(utc.withZone(DateTimeZone.getDefault()))
        notificationTimeZoneInfo.text = getString(R.string.streak_updated_timezone, print)
    }

    private fun initToolbar() {
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)

        //for preventing showing default title
        activity.supportActionBar!!.setDisplayShowTitleEnabled(false)

//        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        activity.supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        activity.supportActionBar!!.elevation = 0f

        toolbarTitle.setText(R.string.profile_title)
    }

    @SuppressLint("SetTextI18n")
    override fun streaksAreLoaded(currentStreak: Int, maxStreak: Int) {
        val suffixCurrent = resources.getQuantityString(R.plurals.day_number, currentStreak)
        val suffixMax = resources.getQuantityString(R.plurals.day_number, maxStreak)

        currentStreakValue.text = String.format("%d %s", currentStreak, suffixCurrent)
        maxStreakValue.text = String.format("%d %s", maxStreak, suffixMax)

        showStreakRoot(true)
    }

    private fun showStreakRoot(needShow: Boolean) {
        if (needShow) {
            currentStreakSuffix.visibility = View.VISIBLE
            currentStreakValue.visibility = View.VISIBLE

            maxStreakSuffix.visibility = View.VISIBLE
            maxStreakValue.visibility = View.VISIBLE
        }
    }

    override fun showLoadingAll() {
        contentRoot.visibility = View.GONE
        profileEmptyUser.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileLoadingView.visibility = View.VISIBLE
    }

    override fun showNameImageShortBio(userViewModel: UserViewModel) {
        profileEmptyUser.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.VISIBLE

        localUserViewModel = userViewModel
        activity.invalidateOptionsMenu()
        if (userViewModel.isMyProfile) {
            streakPresenter.tryShowNotificationSetting()
        }

        mainInfoRoot.visibility = View.VISIBLE
        val nameArray = userViewModel.fullName.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
            if (shortBio.isBlank() && information.isBlank()) {
                //do not show any header
                shortBioInfoContainer.visibility = View.GONE
                //todo: Show placeholder for not my profile
            } else if (shortBio.isBlank() && information.isNotBlank()) {
                //show header with 'information'
                shortBioFirstHeader.setText(R.string.user_info)
                shortBioFirstText.text = information
            } else if (shortBio.isNotBlank() && information.isBlank()) {
                shortBioFirstHeader.setText(R.string.short_bio)
                shortBioFirstText.text = shortBio
            } else if (shortBio.isNotBlank() && information.isNotBlank()) {
                //show general header and all info
                shortBioFirstHeader.setText(R.string.short_bio_and_info)
                shortBioFirstText.text = shortBio
                shortBioSecondHeader.setText(R.string.user_info)
                shortBioSecondText.text = information
            }

            if (shortBio.isNotBlank() || information.isNotBlank()) {
                shortBioInfoContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onInternetFailed() {
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.GONE
        profileEmptyUser.visibility = View.GONE
        profileReportProblem.visibility = View.VISIBLE
    }

    override fun onProfileNotFound() {
        profileLoadingView.visibility = View.GONE
        contentRoot.visibility = View.GONE
        profileReportProblem.visibility = View.GONE
        profileEmptyUser.visibility = View.VISIBLE
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
        val visibility = if (needHide) View.GONE else View.VISIBLE
        notificationIntervalChooser.visibility = visibility
    }

    override fun setNewTimeInterval(timePresentationString: String) {
        notificationIntervalTitle.text = resources.getString(R.string.notification_time, timePresentationString)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTIFICATION_INTERVAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val intervalCode = data!!.getIntExtra(TimeIntervalPickerDialogFragment.resultIntervalCodeKey, TimeIntervalUtil.defaultTimeCode)
                streakPresenter.setStreakTime(intervalCode)
                analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_PROFILE, intervalCode.toString() + "")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_CANCELED_PROFILE)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        if (localUserViewModel != null) {
            Timber.d("onCreateOptionsMenu non null+ %s", this)
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

    companion object {
        private val USER_ID_KEY = "user_id_key"
        private val NOTIFICATION_INTERVAL_REQUEST_CODE = 11

        fun newInstance(): ProfileFragment {
            return newInstance(0)
        }

        fun newInstance(userId: Long): ProfileFragment {
            val args = Bundle()
            args.putLong(USER_ID_KEY, userId)
            val fragment = ProfileFragment()
            fragment.arguments = args
            return fragment
        }
    }

}

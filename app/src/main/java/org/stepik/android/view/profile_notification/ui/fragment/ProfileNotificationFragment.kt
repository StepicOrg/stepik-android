package org.stepik.android.view.profile_notification.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_profile_notification.*
import kotlinx.android.synthetic.main.view_notification_interval_chooser.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.util.collapse
import org.stepic.droid.ui.util.expand
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.profile_notification.ProfileNotificationPresenter
import org.stepik.android.presentation.profile_notification.ProfileNotificationView
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class ProfileNotificationFragment : Fragment(R.layout.fragment_profile_notification), ProfileNotificationView, TimeIntervalPickerDialogFragment.Companion.Callback {
    companion object {
        fun newInstance(userId: Long): Fragment =
            ProfileNotificationFragment()
                .apply {
                    this.userId = userId
                }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var userId by argument<Long>()

    private lateinit var profileNotificationPresenter: ProfileNotificationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        profileNotificationPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ProfileNotificationPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationIntervalChooserContainer.setOnClickListener {
            val supportFragmentManager = fragmentManager
                ?: return@setOnClickListener

            analytic.reportEvent(Analytic.Interaction.CLICK_CHOOSE_NOTIFICATION_INTERVAL)
            val dialog = TimeIntervalPickerDialogFragment.newInstance()
            dialog.setTargetFragment(this@ProfileNotificationFragment, 0)
            dialog.showIfNotExists(supportFragmentManager, TimeIntervalPickerDialogFragment.TAG)
        }

        view.isVisible = false
    }

    private fun injectComponent() {
        App.componentManager()
            .profileComponent(userId)
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        profileNotificationPresenter.attachView(this)
    }

    override fun onStop() {
        profileNotificationPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(profileData: ProfileData?) {
        val isCurrentUser = profileData?.isCurrentUser ?: return

        if (isCurrentUser) {
            view?.isVisible = true
            initTimezone()
            profileNotificationPresenter.tryShowNotificationSetting()
        }
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
            profileNotificationPresenter.switchNotificationStreak(isChecked)
            hideNotificationTime(!isChecked)
        }

        // need to set for show default value, when user enable it
        notificationIntervalTitle.text =
            resources.getString(R.string.notification_time, notificationTimeValue)
    }

    override fun setNewTimeInterval(timePresentationString: String) {
        notificationIntervalTitle.text = resources.getString(R.string.notification_time, timePresentationString)
    }

    override fun hideNotificationTime(needHide: Boolean) {
        if (needHide) {
            notificationIntervalChooserContainer.collapse(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    (notificationStreakSwitch.layoutParams as LinearLayoutCompat.LayoutParams).apply {
                        setMargins(
                            resources.getDimension(R.dimen.profile_block_margin).toInt(),
                            0,
                            resources.getDimension(R.dimen.profile_block_margin).toInt(),
                            resources.getDimension(R.dimen.profile_block_vertical_margin).toInt()
                        )
                    }
                }
                override fun onAnimationStart(animation: Animation?) {}
            })
        } else {
            notificationIntervalChooserContainer.expand(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    notificationTimeZoneInfo.setPadding(
                        resources.getDimension(R.dimen.profile_block_margin).toInt(),
                        0,
                        resources.getDimension(R.dimen.profile_block_margin).toInt(),
                        resources.getDimension(R.dimen.profile_block_vertical_margin).toInt()
                    )
                }
                override fun onAnimationStart(animation: Animation?) {}
            })
        }
    }

    override fun onTimeIntervalPicked(chosenInterval: Int) {
        profileNotificationPresenter.setStreakTime(chosenInterval)
        analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL_PROFILE, chosenInterval.toString() + "")
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
}
package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepik.android.view.course_list.notification.RemindAppNotificationDelegate
import org.stepik.android.view.notification.FcmNotificationHandler
import org.stepik.android.view.notification.FcmNotificationHandlerImpl
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.NotificationPublisher
import org.stepik.android.view.notification.NotificationPublisherImpl
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.StepikNotificationManagerImpl
import org.stepik.android.view.notification.helpers.NotificationHelper
import org.stepik.android.view.notification.helpers.NotificationHelperImpl
import org.stepik.android.view.personal_deadlines.notification.DeadlinesNotificationDelegate
import org.stepik.android.view.splash.notification.RemindRegistrationNotificationDelegate
import org.stepik.android.view.splash.notification.RetentionNotificationDelegate
import org.stepik.android.view.streak.notification.StreakNotificationDelegate

@Module
interface NotificationModule {
    @Binds
    fun bindNotificationPublisher(notificationPublisherImpl: NotificationPublisherImpl): NotificationPublisher

    @Binds
    fun bindStepikNotificationManager(stepikNotificationManagerImpl: StepikNotificationManagerImpl): StepikNotificationManager

    @Binds
    fun bindNotificationHelper(notificationHelperImpl: NotificationHelperImpl): NotificationHelper

    @Binds
    fun bindNotificationResolver(fcmNotificationHandlerImpl: FcmNotificationHandlerImpl): FcmNotificationHandler

    @Binds
    @IntoSet
    fun provideDeadlinesNotificationDelegate(deadlinesNotificationDelegate: DeadlinesNotificationDelegate): NotificationDelegate

    @Binds
    @IntoSet
    fun provideRemindRegistrationNotificationDelegate(remindRegistrationNotificationDelegate: RemindRegistrationNotificationDelegate): NotificationDelegate

    @Binds
    @IntoSet
    fun provideRemindAppNotificationDelegate(remindAppNotificationDelegate: RemindAppNotificationDelegate): NotificationDelegate

    @Binds
    @IntoSet
    fun provideRetentionNotificationDelegate(retentionNotificationDelegate: RetentionNotificationDelegate): NotificationDelegate

    @Binds
    @IntoSet
    fun provideStreakNotificationDelegate(streakNotificationDelegate: StreakNotificationDelegate): NotificationDelegate
}
package org.stepic.droid.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.concurrency.DownloadPoster
import org.stepic.droid.core.CommentManager
import org.stepic.droid.di.certificates.CertificateComponent
import org.stepic.droid.di.course_general.CourseGeneralComponent
import org.stepic.droid.di.feedback.FeedbackComponent
import org.stepic.droid.di.filters.FilterComponent
import org.stepic.droid.di.lesson.LessonComponent
import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.notifications.NotificationsComponent
import org.stepic.droid.di.profile.ProfileComponent
import org.stepic.droid.di.routing.RoutingComponent
import org.stepic.droid.di.storage.StorageComponent
import org.stepic.droid.di.video.VideoComponent
import org.stepic.droid.model.Course
import org.stepic.droid.notifications.HackFcmListener
import org.stepic.droid.notifications.HackerFcmInstanceId
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.receivers.BootCompletedReceiver
import org.stepic.droid.receivers.DownloadClickReceiver
import org.stepic.droid.receivers.DownloadCompleteReceiver
import org.stepic.droid.receivers.InternetConnectionEnabledReceiver
import org.stepic.droid.services.*
import org.stepic.droid.ui.adapters.*
import org.stepic.droid.ui.custom.ExpandableTextView
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.custom.LatexSupportableWebView
import org.stepic.droid.ui.custom_exo.PlaybackControlView
import org.stepic.droid.ui.dialogs.*
import org.stepic.droid.ui.fragments.CommentsFragment
import org.stepic.droid.ui.fragments.DownloadsFragment

@AppSingleton
@Component(dependencies = arrayOf(StorageComponent::class), modules = arrayOf(AppCoreModule::class, RepositoryModule::class, AppStepModule::class))
interface AppCoreComponent {

    @Component.Builder
    interface Builder {
        fun build(): AppCoreComponent

        fun setStorageComponent(storageComponent: StorageComponent): Builder

        @BindsInstance
        fun context(context: Context): Builder
    }

    fun feedbackComponentBuilder(): FeedbackComponent.Builder

    fun loginComponentBuilder(): LoginComponent.Builder

    fun profileComponentBuilder(): ProfileComponent.Builder

    fun certificateComponentBuilder(): CertificateComponent.Builder

    fun courseGeneralComponentBuilder(): CourseGeneralComponent.Builder

    fun lessonComponentBuilder(): LessonComponent.Builder

    fun filterComponentBuilder(): FilterComponent.Builder

    fun videoComponentBuilder(): VideoComponent.Builder

    fun mainScreenComponentBuilder(): MainScreenComponent.Builder

    fun notificationsComponentBuilder(): NotificationsComponent.Builder

    fun routingComponentBuilder(): RoutingComponent.Builder


    fun inject(someActivity: FragmentActivityBase)

    fun inject(adapter: CoursesAdapter)

    fun inject(adapter: Course)

    fun inject(baseFragment: FragmentBase)

    fun inject(baseFragment: DownloadsFragment)

    fun inject(dialogFragment: DiscountingPolicyDialogFragment)

    fun inject(adapter: UnitAdapter)

    fun inject(dialogFragment: LogoutAreYouSureDialog)

    fun inject(dialogFragment: VideoQualityDialog)

    fun inject(loadService: LoadService)

    fun inject(loadService: DeleteService)

    fun inject(downloadCompleteReceiver: DownloadCompleteReceiver)

    fun inject(viewPusher: ViewPusher)

    fun inject(internetConnectionEnabledReceiver: InternetConnectionEnabledReceiver)

    fun inject(socialAuthAdapter: SocialAuthAdapter)

    fun inject(downloadsAdapter: DownloadsAdapter)

    fun inject(clearVideosDialog: ClearVideosDialog)

    fun inject(coursePropertyAdapter: CoursePropertyAdapter)

    fun inject(remindPasswordDialogFragment: RemindPasswordDialogFragment)

    fun inject(service: CancelLoadingService)

    fun inject(downloadClickReceiver: DownloadClickReceiver)

    fun inject(service: HackFcmListener)

    fun inject(instanceIdService: HackerFcmInstanceId)

    fun inject(receiver: NotificationBroadcastReceiver)

    fun inject(needUpdatingDialog: NeedUpdatingDialog)

    fun inject(service: UpdateWithApkService)

    fun inject(downloadPoster: DownloadPoster)

    fun inject(commentManager: CommentManager)

    fun inject(commentsFragment: CommentsFragment)

    fun inject(chooseStorageDialog: ChooseStorageDialog)

    fun inject(wantMoveDataDialog: WantMoveDataDialog)

    fun inject(unauthorizedDialogFragment: UnauthorizedDialogFragment)

    fun inject(dialogFragment: DeleteCommentDialogFragment)

    fun inject(certificateShareDialog: CertificateShareDialog)

    fun inject(sectionAdapter: SectionAdapter)

    fun inject(stepShareDialog: StepShareDialog)

    fun inject(videoQualityDetailedDialog: VideoQualityDetailedDialog)

    fun inject(latexSupportableEnhancedFrameLayout: LatexSupportableEnhancedFrameLayout)

    fun inject(latexSupportableWebView: LatexSupportableWebView)

    fun inject(expandableTextView: ExpandableTextView)

    fun inject(notificationViewHolder: NotificationAdapter.NotificationViewHolder)

    fun inject(app: App)

    fun inject(instructorAdapter: InstructorAdapter)

    fun inject(newUserAlarmService: NewUserAlarmService)

    fun inject(bootCompletedReceiver: BootCompletedReceiver)

    fun inject(timeIntervalPickerDialogFragment: TimeIntervalPickerDialogFragment)

    fun inject(streakAlarmService: StreakAlarmService)

    fun inject(playbackControlView: PlaybackControlView)

    fun inject(videoQualityDialogInPlayer: VideoQualityDialogInPlayer)
}

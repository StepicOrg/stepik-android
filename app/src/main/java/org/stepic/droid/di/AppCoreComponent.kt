package org.stepic.droid.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepic.droid.adaptive.ui.adapters.QuizCardViewHolder
import org.stepic.droid.adaptive.ui.dialogs.AdaptiveLevelDialog
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.code.ui.CodeEditor
import org.stepic.droid.di.adaptive.AdaptiveCourseComponent
import org.stepic.droid.di.analytic.AnalyticModule
import org.stepic.droid.di.catalog.CatalogComponent
import org.stepic.droid.di.certificates.CertificateComponent
import org.stepic.droid.di.course_general.CourseEnrollmentBusModule
import org.stepic.droid.di.course_general.CourseGeneralComponent
import org.stepic.droid.di.downloads.DownloadsComponent
import org.stepic.droid.di.feedback.FeedbackComponent
import org.stepic.droid.di.home.HomeComponent
import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.network.NetworkModule
import org.stepic.droid.di.notifications.NotificationsComponent
import org.stepic.droid.di.profile.ProfileComponent
import org.stepic.droid.di.routing.RoutingComponent
import org.stepic.droid.di.splash.SplashComponent
import org.stepic.droid.di.storage.StorageComponent
import org.stepic.droid.features.achievements.service.AchievementsNotificationService
import org.stepic.droid.features.achievements.ui.adapters.AchievementsAdapter
import org.stepic.droid.features.achievements.ui.adapters.AchievementsTileAdapter
import org.stepic.droid.features.achievements.ui.dialogs.AchievementDetailsDialog
import org.stepik.android.view.personal_deadlines.ui.dialogs.EditDeadlinesDialog
import org.stepik.android.view.personal_deadlines.ui.dialogs.LearningRateDialog
import org.stepik.android.model.Course
import org.stepic.droid.notifications.HackFcmListener
import org.stepic.droid.notifications.HackerFcmInstanceId
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.persistence.di.PersistenceModule
import org.stepic.droid.persistence.service.DownloadCompleteService
import org.stepic.droid.persistence.service.FileTransferService
import org.stepic.droid.receivers.BootCompletedReceiver
import org.stepic.droid.receivers.DownloadClickReceiver
import org.stepic.droid.receivers.InternetConnectionEnabledReceiver
import org.stepic.droid.services.*
import org.stepic.droid.ui.adapters.*
import org.stepic.droid.ui.adapters.viewhoders.CourseItemViewHolder
import org.stepic.droid.ui.custom.*
import org.stepic.droid.ui.dialogs.*
import org.stepic.droid.ui.fragments.StoreManagementFragment
import org.stepic.droid.util.glide.GlideCustomModule
import org.stepik.android.view.injection.billing.BillingModule
import org.stepik.android.view.injection.course.CourseComponent
import org.stepik.android.view.injection.course.CourseRoutingModule
import org.stepik.android.view.injection.course_reviews.ComposeCourseReviewComponent
import org.stepik.android.view.injection.lesson.LessonComponent
import org.stepik.android.view.injection.network.NetworkDataModule
import org.stepik.android.view.injection.personal_deadlines.PersonalDeadlinesDataModule
import org.stepik.android.view.injection.profile.ProfileBusModule
import org.stepik.android.view.injection.profile_edit.ProfileEditComponent
import org.stepik.android.view.injection.progress.ProgressBusModule
import org.stepik.android.view.injection.video_player.VideoPlayerComponent

@AppSingleton
@Component(
    dependencies = [
        StorageComponent::class
    ],
    modules = [
        AppCoreModule::class,
        RepositoryModule::class,
        AnalyticModule::class,
        AppFiltersModule::class,
        GoogleModule::class,
        FirebaseModule::class,
        PersistenceModule::class,
        RecentActiveCourseModule::class,
        NotificationsBadgesModule::class,
        NetworkModule::class,
        NetworkDataModule::class,
        RemoteMessageHandlersModule::class,

        BillingModule::class,

        CourseEnrollmentBusModule::class, // todo unite it in BusModule::class
        ProfileBusModule::class,
        ProgressBusModule::class,
        PersonalDeadlinesDataModule::class,

        CourseRoutingModule::class // todo unite it in RoutingModule::class,
    ]
)
interface AppCoreComponent {

    @Component.Builder
    interface Builder {
        fun build(): AppCoreComponent

        fun setStorageComponent(storageComponent: StorageComponent): Builder

        @BindsInstance
        fun context(context: Context): Builder
    }

    fun splashComponent(): SplashComponent.Builder

    fun feedbackComponentBuilder(): FeedbackComponent.Builder

    fun downloadsComponentBuilder(): DownloadsComponent.Builder

    fun loginComponentBuilder(): LoginComponent.Builder

    fun profileComponentBuilder(): ProfileComponent.Builder

    fun homeComponentBuilder(): HomeComponent.Builder

    fun certificateComponentBuilder(): CertificateComponent.Builder

    fun courseGeneralComponentBuilder(): CourseGeneralComponent.Builder

    fun oldLessonComponentBuilder(): org.stepic.droid.di.lesson.LessonComponent.Builder

    fun mainScreenComponentBuilder(): MainScreenComponent.Builder

    fun notificationsComponentBuilder(): NotificationsComponent.Builder

    fun routingComponentBuilder(): RoutingComponent.Builder

    fun catalogComponentBuilder(): CatalogComponent.Builder

    fun adaptiveCourseComponentBuilder(): AdaptiveCourseComponent.Builder

    fun courseComponentBuilder(): CourseComponent.Builder

    fun videoPlayerComponentBuilder(): VideoPlayerComponent.Builder

    fun profileEditComponentBuilder(): ProfileEditComponent.Builder

    fun composeCourseReviewComponentBuilder(): ComposeCourseReviewComponent.Builder

    fun lessonComponentBuilder(): LessonComponent.Builder

    fun inject(someActivity: FragmentActivityBase)

    fun inject(adapter: StepikRadioGroupAdapter)

    fun inject(adapter: CoursesAdapter)

    fun inject(adapter: Course)

    fun inject(baseFragment: FragmentBase)

    fun inject(dialogFragment: DiscountingPolicyDialogFragment)

    fun inject(dialogFragment: LogoutAreYouSureDialog)

    fun inject(dialogFragment: VideoQualityDialog)

    fun inject(fragment: StoreManagementFragment)

    fun inject(viewPusher: ViewPusher)

    fun inject(internetConnectionEnabledReceiver: InternetConnectionEnabledReceiver)

    fun inject(socialAuthAdapter: SocialAuthAdapter)

    fun inject(downloadsAdapter: DownloadsAdapter)

    fun inject(clearVideosDialog: ClearVideosDialog)

    fun inject(remindPasswordDialogFragment: RemindPasswordDialogFragment)

    fun inject(downloadClickReceiver: DownloadClickReceiver)

    fun inject(service: HackFcmListener)

    fun inject(instanceIdService: HackerFcmInstanceId)

    fun inject(receiver: NotificationBroadcastReceiver)

    fun inject(chooseStorageDialog: ChooseStorageDialog)

    fun inject(wantMoveDataDialog: WantMoveDataDialog)

    fun inject(unauthorizedDialogFragment: UnauthorizedDialogFragment)

    fun inject(dialogFragment: DeleteCommentDialogFragment)

    fun inject(certificateShareDialog: CertificateShareDialog)

    fun inject(stepShareDialog: StepShareDialog)

    fun inject(videoQualityDetailedDialog: VideoQualityDetailedDialog)

    fun inject(coursesLangDialog: CoursesLangDialog)

    fun inject(latexSupportableEnhancedFrameLayout: LatexSupportableEnhancedFrameLayout)

    fun inject(latexSupportableWebView: LatexSupportableWebView)

    fun inject(expandableTextView: ExpandableTextView)

    fun inject(autoCompleteSearchView: AutoCompleteSearchView)

    fun inject(courseItemViewHolder: CourseItemViewHolder)

    fun inject(quizCardViewHolder: QuizCardViewHolder)

    fun inject(adaptiveLevelDialog: AdaptiveLevelDialog)

    fun inject(notificationViewHolder: NotificationAdapter.NotificationViewHolder)

    fun inject(app: App)

    fun inject(searchQueriesAdapter: SearchQueriesAdapter)

    fun inject(alarmService: AlarmService)

    fun inject(bootCompletedReceiver: BootCompletedReceiver)

    fun inject(timeIntervalPickerDialogFragment: TimeIntervalPickerDialogFragment)

    fun inject(videoQualityDialogInPlayer: VideoQualityDialogInPlayer)

    fun inject(rateAppDialogFragment: RateAppDialogFragment)

    fun inject(placeholderTextView: PlaceholderTextView)

    fun inject(codeEditor: CodeEditor)


    fun inject(editDeadlinesDialog: EditDeadlinesDialog)
    fun inject(learningRateDialog: LearningRateDialog)


    fun inject(achievementsTileAdapter: AchievementsTileAdapter)
    fun inject(achievementsAdapter: AchievementsAdapter)
    fun inject(achievementDetailsDialog: AchievementDetailsDialog)


    fun inject(downloadCompleteService: DownloadCompleteService)
    fun inject(fileTransferService: FileTransferService)

    fun inject(achievementsNotificationService: AchievementsNotificationService)

    fun inject(glideCustomModule: GlideCustomModule)
}

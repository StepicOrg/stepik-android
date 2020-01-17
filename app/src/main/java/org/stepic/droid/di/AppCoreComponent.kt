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
import org.stepic.droid.di.course_general.CourseEnrollmentBusModule
import org.stepic.droid.di.course_general.CourseGeneralComponent
import org.stepic.droid.di.home.HomeComponent
import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.notifications.NotificationsComponent
import org.stepic.droid.di.splash.SplashComponent
import org.stepic.droid.di.storage.StorageComponent
import org.stepic.droid.features.achievements.service.AchievementsNotificationService
import org.stepic.droid.notifications.HackFcmListener
import org.stepic.droid.notifications.HackerFcmInstanceId
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.persistence.di.PersistenceModule
import org.stepic.droid.persistence.service.DownloadCompleteService
import org.stepic.droid.persistence.service.FileTransferService
import org.stepic.droid.receivers.DownloadClickReceiver
import org.stepic.droid.receivers.InternetConnectionEnabledReceiver
import org.stepic.droid.ui.adapters.CoursesAdapter
import org.stepic.droid.ui.adapters.NotificationAdapter
import org.stepic.droid.ui.adapters.SearchQueriesAdapter
import org.stepic.droid.ui.adapters.SocialAuthAdapter
import org.stepic.droid.ui.adapters.StepikRadioGroupAdapter
import org.stepic.droid.ui.adapters.viewhoders.CourseItemViewHolder
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import org.stepic.droid.ui.custom.ExpandableTextView
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.custom.LatexSupportableWebView
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepic.droid.ui.dialogs.CertificateShareDialog
import org.stepic.droid.ui.dialogs.ChooseStorageDialog
import org.stepic.droid.ui.dialogs.ClearVideosDialog
import org.stepic.droid.ui.dialogs.CoursesLangDialog
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.ui.dialogs.RemindPasswordDialogFragment
import org.stepic.droid.ui.dialogs.StepShareDialog
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog
import org.stepic.droid.ui.dialogs.VideoQualityDialog
import org.stepic.droid.ui.dialogs.VideoQualityDialogInPlayer
import org.stepic.droid.ui.dialogs.WantMoveDataDialog
import org.stepic.droid.ui.fragments.StoreManagementFragment
import org.stepic.droid.util.glide.GlideCustomModule
import org.stepik.android.model.Course
import org.stepik.android.view.app_rating.ui.dialog.RateAppDialog
import org.stepik.android.view.injection.achievements.AchievementsComponent
import org.stepik.android.view.injection.certificate.CertificateComponent
import org.stepik.android.view.injection.comment.CommentsComponent
import org.stepik.android.view.injection.comment.ComposeCommentComponent
import org.stepik.android.view.injection.course.CourseComponent
import org.stepik.android.view.injection.course.CourseRoutingModule
import org.stepik.android.view.injection.course_collection.CourseCollectionDataModule
import org.stepik.android.view.injection.course_reviews.ComposeCourseReviewComponent
import org.stepik.android.view.injection.device.DeviceDataModule
import org.stepik.android.view.injection.download.DownloadComponent
import org.stepik.android.view.injection.email_address.EmailAddressDataModule
import org.stepik.android.view.injection.feedback.FeedbackComponent
import org.stepik.android.view.injection.font_size_settings.FontSizeComponent
import org.stepik.android.view.injection.lesson.LessonComponent
import org.stepik.android.view.injection.network.NetworkDataModule
import org.stepik.android.view.injection.network.NetworkModule
import org.stepik.android.view.injection.notification.NotificationDataModule
import org.stepik.android.view.injection.personal_deadlines.PersonalDeadlinesDataModule
import org.stepik.android.view.injection.profile.ProfileBusModule
import org.stepik.android.view.injection.profile.ProfileComponent
import org.stepik.android.view.injection.profile_edit.ProfileEditComponent
import org.stepik.android.view.injection.progress.ProgressBusModule
import org.stepik.android.view.injection.search.SearchDataModule
import org.stepik.android.view.injection.settings.SettingsComponent
import org.stepik.android.view.injection.social_profile.SocialProfileComponent
import org.stepik.android.view.injection.step.StepComponent
import org.stepik.android.view.injection.step.StepDiscussionBusModule
import org.stepik.android.view.injection.step_quiz.StepQuizBusModule
import org.stepik.android.view.injection.story.StoryDataModule
import org.stepik.android.view.injection.submission.SubmissionComponent
import org.stepik.android.view.injection.user_activity.UserActivityDataModule
import org.stepik.android.view.injection.user_courses.UserCoursesDataModule
import org.stepik.android.view.injection.video_player.VideoPlayerComponent
import org.stepik.android.view.injection.view_assignment.ViewAssignmentBusModule
import org.stepik.android.view.injection.view_assignment.ViewAssignmentComponent
import org.stepik.android.view.latex.ui.widget.LatexView
import org.stepik.android.view.notification.service.BootCompleteService
import org.stepik.android.view.notification.service.NotificationAlarmService
import org.stepik.android.view.personal_deadlines.ui.dialogs.EditDeadlinesDialog
import org.stepik.android.view.personal_deadlines.ui.dialogs.LearningRateDialog

@AppSingleton
@Component(
    dependencies = [
        StorageComponent::class
    ],
    modules = [
        AppCoreModule::class,
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

        CourseEnrollmentBusModule::class, // todo unite it in BusModule::class
        ProfileBusModule::class,
        ProgressBusModule::class,
        ViewAssignmentBusModule::class,
        StepDiscussionBusModule::class,
        StepQuizBusModule::class,
        PersonalDeadlinesDataModule::class,

        CourseRoutingModule::class, // todo unite it in RoutingModule::class
        NotificationModule::class,

        StoryDataModule::class,
        DeviceDataModule::class,
        UserActivityDataModule::class,
        NotificationDataModule::class,
        EmailAddressDataModule::class,
        SearchDataModule::class,
        UserCoursesDataModule::class,
        CourseCollectionDataModule::class
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

    fun loginComponentBuilder(): LoginComponent.Builder

    fun homeComponentBuilder(): HomeComponent.Builder

    fun courseGeneralComponentBuilder(): CourseGeneralComponent.Builder

    fun mainScreenComponentBuilder(): MainScreenComponent.Builder

    fun notificationsComponentBuilder(): NotificationsComponent.Builder

    fun catalogComponentBuilder(): CatalogComponent.Builder

    fun adaptiveCourseComponentBuilder(): AdaptiveCourseComponent.Builder

    fun courseComponentBuilder(): CourseComponent.Builder

    fun videoPlayerComponentBuilder(): VideoPlayerComponent.Builder

    fun profileEditComponentBuilder(): ProfileEditComponent.Builder

    fun composeCourseReviewComponentBuilder(): ComposeCourseReviewComponent.Builder

    fun viewAssignmentComponentBuilder(): ViewAssignmentComponent.Builder

    fun lessonComponentBuilder(): LessonComponent.Builder

    fun stepComponentBuilder(): StepComponent.Builder

    fun certificatesComponentBuilder(): CertificateComponent.Builder

    fun composeCommentComponentBuilder(): ComposeCommentComponent.Builder

    fun commentsComponentBuilder(): CommentsComponent.Builder

    fun downloadComponentBuilder(): DownloadComponent.Builder

    fun fontSizeComponentBuilder(): FontSizeComponent.Builder

    fun submissionComponentBuilder(): SubmissionComponent.Builder

    fun achievementsComponentBuilder(): AchievementsComponent.Builder

    fun profileComponentBuilder(): ProfileComponent.Builder

    fun settingsComponentBuilder(): SettingsComponent.Builder

    fun socialProfileComponentBuilder(): SocialProfileComponent.Builder

    fun inject(someActivity: FragmentActivityBase)

    fun inject(adapter: StepikRadioGroupAdapter)

    fun inject(adapter: CoursesAdapter)

    fun inject(adapter: Course)

    fun inject(baseFragment: FragmentBase)

    fun inject(dialogFragment: LogoutAreYouSureDialog)

    fun inject(dialogFragment: VideoQualityDialog)

    fun inject(fragment: StoreManagementFragment)

    fun inject(internetConnectionEnabledReceiver: InternetConnectionEnabledReceiver)

    fun inject(socialAuthAdapter: SocialAuthAdapter)

    fun inject(clearVideosDialog: ClearVideosDialog)

    fun inject(remindPasswordDialogFragment: RemindPasswordDialogFragment)

    fun inject(downloadClickReceiver: DownloadClickReceiver)

    fun inject(service: HackFcmListener)

    fun inject(instanceIdService: HackerFcmInstanceId)

    fun inject(receiver: NotificationBroadcastReceiver)

    fun inject(chooseStorageDialog: ChooseStorageDialog)

    fun inject(wantMoveDataDialog: WantMoveDataDialog)

    fun inject(unauthorizedDialogFragment: UnauthorizedDialogFragment)

    fun inject(certificateShareDialog: CertificateShareDialog)

    fun inject(stepShareDialog: StepShareDialog)

    fun inject(videoQualityDetailedDialog: VideoQualityDetailedDialog)

    fun inject(coursesLangDialog: CoursesLangDialog)

    fun inject(latexSupportableEnhancedFrameLayout: LatexSupportableEnhancedFrameLayout)

    fun inject(latexSupportableWebView: LatexSupportableWebView)

    fun inject(latexView: LatexView)

    fun inject(expandableTextView: ExpandableTextView)

    fun inject(autoCompleteSearchView: AutoCompleteSearchView)

    fun inject(courseItemViewHolder: CourseItemViewHolder)

    fun inject(quizCardViewHolder: QuizCardViewHolder)

    fun inject(adaptiveLevelDialog: AdaptiveLevelDialog)

    fun inject(notificationViewHolder: NotificationAdapter.NotificationViewHolder)

    fun inject(app: App)

    fun inject(searchQueriesAdapter: SearchQueriesAdapter)

    fun inject(timeIntervalPickerDialogFragment: TimeIntervalPickerDialogFragment)

    fun inject(videoQualityDialogInPlayer: VideoQualityDialogInPlayer)

    fun inject(rateAppDialog: RateAppDialog)

    fun inject(placeholderTextView: PlaceholderTextView)

    fun inject(codeEditor: CodeEditor)

    fun inject(editDeadlinesDialog: EditDeadlinesDialog)
    fun inject(learningRateDialog: LearningRateDialog)

    fun inject(downloadCompleteService: DownloadCompleteService)
    fun inject(fileTransferService: FileTransferService)

    fun inject(achievementsNotificationService: AchievementsNotificationService)

    fun inject(glideCustomModule: GlideCustomModule)

    fun inject(notificationAlarmService: NotificationAlarmService)

    fun inject(bootCompleteService: BootCompleteService)
}

package org.stepic.droid.core.components;

import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.DownloadPoster;
import org.stepic.droid.concurrency.tasks.FromDbUnitLessonTask;
import org.stepic.droid.concurrency.tasks.ToDbUnitLessonTask;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.core.MyPhoneStateListener;
import org.stepic.droid.core.Shell;
import org.stepic.droid.core.modules.AppCoreModule;
import org.stepic.droid.core.modules.CertificateModule;
import org.stepic.droid.core.modules.CourseDetailModule;
import org.stepic.droid.core.modules.CourseListModule;
import org.stepic.droid.core.modules.FilterModule;
import org.stepic.droid.core.modules.NotificationModule;
import org.stepic.droid.core.modules.ProfileModule;
import org.stepic.droid.core.modules.SectionModule;
import org.stepic.droid.core.modules.StepModule;
import org.stepic.droid.core.modules.VideoModule;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.notifications.HackFcmListener;
import org.stepic.droid.notifications.HackerFcmInstanceId;
import org.stepic.droid.notifications.NotificationBroadcastReceiver;
import org.stepic.droid.receivers.DownloadClickReceiver;
import org.stepic.droid.receivers.DownloadCompleteReceiver;
import org.stepic.droid.receivers.InternetConnectionEnabledReceiver;
import org.stepic.droid.services.CancelLoadingService;
import org.stepic.droid.services.DeleteService;
import org.stepic.droid.services.LoadService;
import org.stepic.droid.services.UpdateAppService;
import org.stepic.droid.services.UpdateWithApkService;
import org.stepic.droid.services.ViewPusher;
import org.stepic.droid.ui.activities.SectionActivity;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.ui.adapters.CoursesAdapter;
import org.stepic.droid.ui.adapters.DownloadsAdapter;
import org.stepic.droid.ui.adapters.NotificationAdapter;
import org.stepic.droid.ui.adapters.SectionAdapter;
import org.stepic.droid.ui.adapters.SocialAuthAdapter;
import org.stepic.droid.ui.adapters.SortStepAdapter;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.ui.custom.ExpandableTextView;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.ui.custom.LatexSupportableWebView;
import org.stepic.droid.ui.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.ui.dialogs.CertificateShareDialog;
import org.stepic.droid.ui.dialogs.ChooseCalendarDialog;
import org.stepic.droid.ui.dialogs.ChooseStorageDialog;
import org.stepic.droid.ui.dialogs.ClearVideosDialog;
import org.stepic.droid.ui.dialogs.DeleteCommentDialogFragment;
import org.stepic.droid.ui.dialogs.DiscountingPolicyDialogFragment;
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.ui.dialogs.NeedUpdatingDialog;
import org.stepic.droid.ui.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.ui.dialogs.StepShareDialog;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog;
import org.stepic.droid.ui.dialogs.VideoQualityDialog;
import org.stepic.droid.ui.dialogs.WantMoveDataDialog;
import org.stepic.droid.ui.fragments.CommentsFragment;
import org.stepic.droid.web.RetrofitRESTApi;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppCoreModule.class})
public interface AppCoreComponent {

    ProfileComponent plus(ProfileModule profileModule);

    SectionComponent plus(SectionModule module);

    CourseDetailComponent plus(CourseDetailModule module);

    CertificateComponent plus(CertificateModule module);

    StepComponent plus(StepModule module);

    VideoComponent plus(VideoModule module);

    FilterComponent plus(FilterModule module);

    CourseListComponent plus(CourseListModule module);

    NotificationComponent plus(NotificationModule module);

    void inject(FragmentActivityBase someActivity);

    void inject(SectionActivity someActivity);

    void inject(Shell injectAllToShell);

    void inject(CoursesAdapter adapter);

    void inject(Course adapter);

    void inject(FragmentBase baseFragment);

    void inject(RetrofitRESTApi api);

    void inject(DiscountingPolicyDialogFragment dialogFragment);

    void inject(Section section);

    void inject(UnitAdapter adapter);

    void inject(LogoutAreYouSureDialog dialogFragment);

    void inject(VideoQualityDialog dialogFragment);


    //All Tasks:

    void inject(FromDbUnitLessonTask stepicTask);

    void inject(ToDbUnitLessonTask stepicTask);

    void inject(ChooseCalendarDialog calendarDialog);


    void inject(AllowMobileDataDialogFragment allowMobileDataDialogFragment);

    void inject(LoadService loadService);

    void inject(DeleteService loadService);

    void inject(UpdateAppService updateAppService);

    void inject(DownloadCompleteReceiver downloadCompleteReceiver);

    void inject(ViewPusher viewPusher);

    void inject(InternetConnectionEnabledReceiver internetConnectionEnabledReceiver);

    void inject(SocialAuthAdapter socialAuthAdapter);

    void inject(DownloadsAdapter downloadsAdapter);

    void inject(ClearVideosDialog clearVideosDialog);

    void inject(CoursePropertyAdapter coursePropertyAdapter);

    void inject(RemindPasswordDialogFragment remindPasswordDialogFragment);

    void inject(CancelLoadingService service);

    void inject(DownloadClickReceiver downloadClickReceiver);

    void inject(MyPhoneStateListener receiver);

    void inject(HackFcmListener service);

    void inject(HackerFcmInstanceId instanceIdService);

    void inject(NotificationBroadcastReceiver receiver);

    void inject(NeedUpdatingDialog needUpdatingDialog);

    void inject(UpdateWithApkService service);

    void inject(DownloadPoster downloadPoster);

    void inject(DownloadsAdapter.CancelVideoDialog cancelVideoDialog);

    void inject(CommentManager commentManager);

    void inject(CommentsFragment commentsFragment);

    void inject(ChooseStorageDialog chooseStorageDialog);

    void inject(WantMoveDataDialog wantMoveDataDialog);

    void inject(UnauthorizedDialogFragment unauthorizedDialogFragment);

    void inject(DeleteCommentDialogFragment dialogFragment);

    void inject(CertificateShareDialog certificateShareDialog);

    void inject(SectionAdapter sectionAdapter);

    void inject(StepShareDialog stepShareDialog);

    void inject(VideoQualityDetailedDialog videoQualityDetailedDialog);

    void inject(LatexSupportableEnhancedFrameLayout latexSupportableEnhancedFrameLayout);

    void inject(LatexSupportableWebView latexSupportableWebView);

    void inject(ExpandableTextView expandableTextView);

    void inject(SortStepAdapter sortStepAdapter);

    void inject(NotificationAdapter.NotificationViewHolder notificationViewHolder);

    void inject(MainApplication mainApplication);
}

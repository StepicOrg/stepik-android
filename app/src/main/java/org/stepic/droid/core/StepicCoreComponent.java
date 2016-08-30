package org.stepic.droid.core;

import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.concurrency.DownloadPoster;
import org.stepic.droid.concurrency.tasks.FromDbCoursesTask;
import org.stepic.droid.concurrency.tasks.FromDbSectionTask;
import org.stepic.droid.concurrency.tasks.FromDbStepTask;
import org.stepic.droid.concurrency.tasks.FromDbUnitLessonTask;
import org.stepic.droid.concurrency.tasks.ToDbCoursesTask;
import org.stepic.droid.concurrency.tasks.ToDbSectionTask;
import org.stepic.droid.concurrency.tasks.ToDbUnitLessonTask;
import org.stepic.droid.concurrency.tasks.UpdateCourseTask;
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
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.activities.SectionActivity;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.ui.adapters.DownloadsAdapter;
import org.stepic.droid.ui.adapters.MyCoursesAdapter;
import org.stepic.droid.ui.adapters.SectionAdapter;
import org.stepic.droid.ui.adapters.SocialAuthAdapter;
import org.stepic.droid.ui.adapters.StepFragmentAdapter;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.ui.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.ui.dialogs.CertificateShareDialog;
import org.stepic.droid.ui.dialogs.ChooseCalendarDialog;
import org.stepic.droid.ui.dialogs.ChooseStorageDialog;
import org.stepic.droid.ui.dialogs.ClearVideosDialog;
import org.stepic.droid.ui.dialogs.DeleteCommentDialogFragment;
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.ui.dialogs.NeedUpdatingDialog;
import org.stepic.droid.ui.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment;
import org.stepic.droid.ui.dialogs.VideoQualityDialog;
import org.stepic.droid.ui.dialogs.WantMoveDataDialog;
import org.stepic.droid.ui.fragments.CommentsFragment;
import org.stepic.droid.web.RetrofitRESTApi;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StepicDefaultModule.class})
public interface StepicCoreComponent {

    SectionComponent plus(SectionModule module);

    CourseDetailComponent plus(CourseDetailModule module);

    CertificateComponent plus(CertificateModule module);

    StepComponent plus(StepModule module);

    FilterComponent plus(FilterModule module);

    CourseListComponent plus(CourseListModule module);

    void inject(FragmentActivityBase someActivity);

    void inject(SectionActivity someActivity);

    void inject(Shell injectAllToShell);

    void inject(MyCoursesAdapter adapter);

    void inject(Course adapter);

    void inject(FragmentBase baseFragment);

    void inject(RetrofitRESTApi api);

    void inject(Section section);

    void inject(UnitAdapter adapter);

    void inject(StepFragmentAdapter adapter);

    void inject(LogoutAreYouSureDialog dialogFragment);

    void inject(VideoQualityDialog dialogFragment);


    //All Tasks:

    void inject(ToDbCoursesTask stepicTask);

    void inject(UpdateCourseTask stepicTask);

    void inject(FromDbSectionTask stepicTask);

    void inject(ToDbSectionTask stepicTask);

    void inject(FromDbUnitLessonTask stepicTask);

    void inject(ToDbUnitLessonTask stepicTask);

    void inject(ChooseCalendarDialog calendarDialog);

    void inject(FromDbStepTask stepicTask);

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

    void inject(DatabaseFacade databaseFacade);

    void inject(CancelLoadingService service);

    void inject(DownloadClickReceiver downloadClickReceiver);

    void inject(FromDbCoursesTask fromDbCoursesTask);

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
}

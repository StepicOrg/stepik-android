package org.stepic.droid.core;

import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
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
import org.stepic.droid.notifications.NotificationBroadcastReceiver;
import org.stepic.droid.notifications.RegistrationIntentService;
import org.stepic.droid.notifications.StepicGcmListenerService;
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
import org.stepic.droid.util.ImageOnDisk;
import org.stepic.droid.view.adapters.CoursePropertyAdapter;
import org.stepic.droid.view.adapters.DownloadsAdapter;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.view.adapters.SectionAdapter;
import org.stepic.droid.view.adapters.SocialAuthAdapter;
import org.stepic.droid.view.adapters.StepFragmentAdapter;
import org.stepic.droid.view.adapters.UnitAdapter;
import org.stepic.droid.view.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.view.dialogs.ClearVideosDialog;
import org.stepic.droid.view.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.view.dialogs.NeedUpdatingDialog;
import org.stepic.droid.view.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.view.dialogs.VideoQualityDialog;
import org.stepic.droid.web.RetrofitRESTApi;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StepicDefaultModule.class})
public interface StepicCoreComponent {
    void inject(FragmentActivityBase someActivity);

    void inject(Shell injectAllToShell);

    void inject(MyCoursesAdapter adapter);

    void inject(Course adapter);

    void inject(FragmentBase baseFragment);

    void inject(RetrofitRESTApi api);

    void inject(Section section);

    void inject(SectionAdapter adapter);

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

    void inject(FromDbStepTask stepicTask);

    void inject(AllowMobileDataDialogFragment allowMobileDataDialogFragment);

    void inject(LoadService loadService);

    void inject(DeleteService loadService);

    void inject(UpdateAppService updateAppService);

    void inject(DownloadCompleteReceiver downloadCompleteReceiver);

    void inject(ImageOnDisk imageOnDisk);

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

    void inject(RegistrationIntentService service);

    void inject(StepicGcmListenerService listenerService);

    void inject (NotificationBroadcastReceiver receiver);

    void inject(NeedUpdatingDialog needUpdatingDialog);

    void inject(UpdateWithApkService service);
}

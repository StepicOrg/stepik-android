package org.stepic.droid.core;

import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.concurrency.FromDbSectionTask;
import org.stepic.droid.concurrency.FromDbStepTask;
import org.stepic.droid.concurrency.FromDbUnitLessonTask;
import org.stepic.droid.concurrency.ToDbCachedVideo;
import org.stepic.droid.concurrency.ToDbCoursesTask;
import org.stepic.droid.concurrency.ToDbSectionTask;
import org.stepic.droid.concurrency.ToDbStepTask;
import org.stepic.droid.concurrency.ToDbUnitLessonTask;
import org.stepic.droid.concurrency.UpdateCourseTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.receivers.DownloadClickReceiver;
import org.stepic.droid.receivers.DownloadCompleteReceiver;
import org.stepic.droid.receivers.InternetConnectionEnabledReceiver;
import org.stepic.droid.services.CancelLoadingService;
import org.stepic.droid.services.DeleteService;
import org.stepic.droid.services.LoadService;
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
import org.stepic.droid.view.dialogs.ClearCacheDialogFragment;
import org.stepic.droid.view.dialogs.LogoutAreYouSureDialog;
import org.stepic.droid.view.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.view.dialogs.VideoQualityDialog;
import org.stepic.droid.view.fragments.DownloadsFragment;
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

    void inject(ClearCacheDialogFragment dialogFragment);

    void inject(LogoutAreYouSureDialog dialogFragment);

    void inject(VideoQualityDialog dialogFragment);


    //All Tasks:

    void inject(ToDbCoursesTask stepicTask);

    void inject(UpdateCourseTask stepicTask);

    void inject(FromDbSectionTask stepicTask);

    void inject(ToDbSectionTask stepicTask);

    void inject(FromDbUnitLessonTask stepicTask);

    void inject(ToDbUnitLessonTask stepicTask);

    void inject(ToDbStepTask stepicTask);

    void inject(FromDbStepTask stepicTask);

    void inject(ToDbCachedVideo stepicTask);

    void inject(AllowMobileDataDialogFragment allowMobileDataDialogFragment);

    void inject(LoadService loadService);

    void inject(DeleteService loadService);

    void inject(DownloadCompleteReceiver downloadCompleteReceiver);

    void inject(ImageOnDisk imageOnDisk);

    void inject(ViewPusher viewPusher);

    void inject(InternetConnectionEnabledReceiver internetConnectionEnabledReceiver);

    void inject(SocialAuthAdapter socialAuthAdapter);

    void inject(DownloadsAdapter downloadsAdapter);

    void inject(DownloadsFragment.ClearVideosDialog clearVideosDialog);

    void inject(CoursePropertyAdapter coursePropertyAdapter);

    void inject(RemindPasswordDialogFragment remindPasswordDialogFragment);

    void inject(DatabaseFacade databaseFacade);

    void inject(CancelLoadingService service);

    void inject(DownloadClickReceiver downloadClickReceiver);
}

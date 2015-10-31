package org.stepic.droid.core;

import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.concurrency.FromDbCoursesTask;
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
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.view.adapters.SectionAdapter;
import org.stepic.droid.view.adapters.StepFragmentAdapter;
import org.stepic.droid.view.adapters.UnitAdapter;
import org.stepic.droid.view.dialogs.AllowMobileDataDialogFragment;
import org.stepic.droid.view.dialogs.ClearCacheDialogFragment;
import org.stepic.droid.web.HttpManager;
import org.stepic.droid.web.RetrofitRESTApi;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StepicDefaultModule.class})
public interface StepicCoreComponent {
    void inject(FragmentActivityBase someActivity);

    void inject(Shell injectAllToShell);

    void inject(HttpManager httpManager);

    void inject(MyCoursesAdapter adapter);

    void inject(Course adapter);

    void inject(FragmentBase baseFragment);

    void inject(RetrofitRESTApi api);

    void inject(Section section);

    void inject(SectionAdapter adapter);

    void inject(UnitAdapter adapter);

    void inject(StepFragmentAdapter adapter);

    void inject(ClearCacheDialogFragment dialogFragment);


    //All Tasks:

    void inject(FromDbCoursesTask stepicTask);

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
}

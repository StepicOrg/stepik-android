package org.stepic.droid.core;

import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.concurrency.DbCoursesTask;
import org.stepic.droid.concurrency.LoadingCoursesTask;
import org.stepic.droid.concurrency.LoadingProfileInformation;
import org.stepic.droid.concurrency.LoginTask;
import org.stepic.droid.concurrency.RegistrationTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.HttpManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StepicDefaultModule.class})
public interface StepicCoreComponent {
    void inject(StepicBaseFragmentActivity someActivity);
    void inject(Shell injectAllToShell);
    void inject(Api injectToAPI);
    void inject(HttpManager httpManager);
    void inject (MyCoursesAdapter adapter);
    void inject (Course adapter);
    void inject (StepicBaseFragment baseFragment);



    //All Tasks:
    void inject(LoginTask stepicTask);
    void inject(RegistrationTask stepicTask);
    void inject(LoadingCoursesTask stepicTask);
    void inject(LoadingProfileInformation stepicTask);
    void inject(DbCoursesTask stepicTask);
}

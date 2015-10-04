package org.stepic.droid.core;

import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.concurrency.FromDbCoursesTask;
import org.stepic.droid.concurrency.LoginTask;
import org.stepic.droid.concurrency.RegistrationTask;
import org.stepic.droid.concurrency.ToDbCoursesTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.view.adapters.MyCoursesAdapter;
import org.stepic.droid.web.HttpManager;
import org.stepic.droid.web.RetrofitRESTApi;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StepicDefaultModule.class})
public interface StepicCoreComponent {
    void inject(StepicBaseFragmentActivity someActivity);

    void inject(Shell injectAllToShell);

    void inject(HttpManager httpManager);

    void inject(MyCoursesAdapter adapter);

    void inject(Course adapter);

    void inject(StepicBaseFragment baseFragment);

    void inject(RetrofitRESTApi api);


    //All Tasks:
    void inject(LoginTask stepicTask);

    void inject(RegistrationTask stepicTask);

    void inject(FromDbCoursesTask stepicTask);

    void inject(ToDbCoursesTask stepicTask);
}

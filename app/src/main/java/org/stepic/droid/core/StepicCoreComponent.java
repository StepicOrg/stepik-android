package org.stepic.droid.core;

import org.stepic.droid.base.StepicBaseFragmentActivity;
import org.stepic.droid.concurrency.LoginTask;
import org.stepic.droid.concurrency.RegistrationTask;
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


    //All Tasks:
    void inject(LoginTask stepicTask);
    void inject(RegistrationTask stepicTask);
}

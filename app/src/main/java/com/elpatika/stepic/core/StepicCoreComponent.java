package com.elpatika.stepic.core;

import com.elpatika.stepic.base.StepicBaseFragmentActivity;
import com.elpatika.stepic.concurrency.LoginTask;
import com.elpatika.stepic.concurrency.RegistrationTask;
import com.elpatika.stepic.web.Api;
import com.elpatika.stepic.web.HttpManager;

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

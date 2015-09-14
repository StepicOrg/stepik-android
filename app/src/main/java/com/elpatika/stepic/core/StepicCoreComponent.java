package com.elpatika.stepic.core;

import com.elpatika.stepic.base.StepicBaseFragmentActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StepicDefaultModule.class})
public interface StepicCoreComponent {
    void inject(StepicBaseFragmentActivity someActivity);
    void inject(Shell injectAllToShell);
}

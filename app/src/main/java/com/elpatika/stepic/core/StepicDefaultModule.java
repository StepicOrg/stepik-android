package com.elpatika.stepic.core;

import com.google.inject.AbstractModule;


public class StepicDefaultModule extends AbstractModule {

    @Override
    public void configure() {
        bind(IScreenProvider.class).to(ScreenProvider.class);
        bind(IShell.class).to(Shell.class);
    }
}

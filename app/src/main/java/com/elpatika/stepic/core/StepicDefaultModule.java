package com.elpatika.stepic.core;

import com.google.inject.AbstractModule;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public class StepicDefaultModule extends AbstractModule {

    @Override
    public void configure() {
        bind(IScreenProvider.class).to(ScreenProvider.class);
        bind(IShell.class).to(Shell.class);
    }
}

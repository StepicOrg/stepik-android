package com.elpatika.stepic.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;



@Singleton
public class Shell implements IShell {

    @Inject
    IScreenProvider screenProvider;

    @Override
    public IScreenProvider getScreenProvider() {
        return screenProvider;
    }
}

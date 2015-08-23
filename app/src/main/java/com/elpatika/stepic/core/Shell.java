package com.elpatika.stepic.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by kirillmakarov on 23.08.15.
 */

@Singleton
public class Shell implements IShell {

    @Inject
    IScreenProvider screenProvider;

    @Override
    public IScreenProvider getScreenProvider() {
        return screenProvider;
    }
}

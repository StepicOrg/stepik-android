package com.elpatika.stepic.core;

import android.app.Activity;
import android.content.Context;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public interface IScreenProvider {
    void showLaunchScreen(Context context, boolean overrideAnimation);
    void showRegistration(Activity sourceActivity);
}

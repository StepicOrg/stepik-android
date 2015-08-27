package com.elpatika.stepic.core;

import android.app.Activity;
import android.content.Context;

public interface IScreenProvider {
    void showLaunchScreen(Context context, boolean overrideAnimation);
    void showRegistration(Activity sourceActivity);
}

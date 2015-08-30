package com.elpatika.stepic.core;

import android.app.Activity;
import android.content.Context;

public interface IScreenManager {
    void showLaunchScreen(Context context, boolean overrideAnimation);
    void showRegistration(Activity sourceActivity);
    void showLogin(Context sourceActivity);

    void showMainFeed(Context sourceActivity);

}

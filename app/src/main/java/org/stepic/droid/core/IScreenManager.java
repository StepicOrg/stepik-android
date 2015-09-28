package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;

public interface IScreenManager {
    void showLaunchScreen(Context context, boolean overrideAnimation);
    void showRegistration(Activity sourceActivity);
    void showLogin(Context sourceActivity);

    void showMainFeed(Context sourceActivity);

    void showCourse (Context sourceActivity, @NotNull Course course);

}

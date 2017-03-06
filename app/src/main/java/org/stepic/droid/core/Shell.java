package org.stepic.droid.core;


import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.Api;

@Deprecated
public interface Shell {
    ScreenManager getScreenProvider();
    Api getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
}

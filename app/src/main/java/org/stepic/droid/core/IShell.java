package org.stepic.droid.core;


import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

@Deprecated
public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
}

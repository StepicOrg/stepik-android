package org.stepic.droid.core;


import org.stepic.droid.util.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
}

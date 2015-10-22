package org.stepic.droid.core;


import android.content.Context;

import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
    Context getContext();
}

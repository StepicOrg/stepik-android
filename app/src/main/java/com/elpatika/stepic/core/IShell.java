package com.elpatika.stepic.core;


import com.elpatika.stepic.util.SharedPreferenceHelper;
import com.elpatika.stepic.web.IApi;

public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
    SharedPreferenceHelper getSharedPreferenceHelper();
}

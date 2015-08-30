package com.elpatika.stepic.core;


import com.elpatika.stepic.web.IApi;

public interface IShell {

    IScreenManager getScreenProvider();
    IApi getApi();
}

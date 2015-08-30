package com.elpatika.stepic.configuration;

import com.google.inject.Singleton;

@Singleton
public class ConfigForTest implements IConfig{

    @Override
    public String getOAuthClientId() {
        return "BJS1K4bosMQFkxteFvdIXfwiRBAsX2ZQws3I9cf0";
    }

    @Override
    public String getBaseUrl() {
        return "https://stepic.org/oauth2/token/";
    }
}

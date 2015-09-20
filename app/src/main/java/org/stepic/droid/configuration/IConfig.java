package org.stepic.droid.configuration;

public interface IConfig {
    String getOAuthClientId();
    String getBaseUrl();
    String getOAuthClientSecret();
    String getGrantType();
    String getRefreshGrantType();
    String getDatePattern();
    String getDatePatternForView();
}

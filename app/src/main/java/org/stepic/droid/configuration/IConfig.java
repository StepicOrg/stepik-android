package org.stepic.droid.configuration;

import org.stepic.droid.web.IApi;

public interface IConfig {
    String getOAuthClientId(IApi.TokenType type);
    String getBaseUrl();
    String getOAuthClientSecret(IApi.TokenType type);
    String getGrantType(IApi.TokenType type);
    String getRefreshGrantType();
    String getDatePattern();
    String getDatePatternForView();
    String getIDSParam();

    String getRedirectUri();
    String getZendeskHost();
    boolean isUserCanDropCourse();
    boolean isCustomUpdateEnable();
    String getUpdateEndpoint();

    String getFirebaseDomain();

    String getGoogleServerClientId();

    String getIOSOauth2Id();
}

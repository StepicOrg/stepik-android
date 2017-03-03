package org.stepic.droid.configuration;

import org.stepic.droid.web.Api;

public interface Config {
    String getOAuthClientId(Api.TokenType type);

    String getBaseUrl();

    String getOAuthClientSecret(Api.TokenType type);

    String getGrantType(Api.TokenType type);

    String getRefreshGrantType();

    String getDatePatternForView();

    String getIDSParam();

    String getRedirectUri();

    String getZendeskHost();

    boolean isUserCanDropCourse();

    boolean isCustomUpdateEnable();

    String getUpdateEndpoint();

    String getFirebaseDomain();

    String getGoogleServerClientId();

    String getPrivacyPolicyUrl();

    String getTermsOfServiceUrl();

    String getMixpanelToken();

    String getCsrfTokenCookieName();

    String getSessionCookieName();
}

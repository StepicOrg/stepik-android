package org.stepic.droid.configuration;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.web.Api;

public interface Config {

    String getOAuthClientId(@NotNull Api.TokenType type);

    String getBaseUrl();

    String getOAuthClientSecret(Api.TokenType type);

    String getGrantType(Api.TokenType type);

    String getRefreshGrantType();

    String getRedirectUri();

    String getZendeskHost();

    boolean isUserCanDropCourse();

    boolean isCustomUpdateEnable();

    String getUpdateEndpoint();

    String getFirebaseDomain();

    String getGoogleServerClientId();

    String getPrivacyPolicyUrl();

    String getTermsOfServiceUrl();

    String getCsrfTokenCookieName();

    String getSessionCookieName();

    String getAmplitudeApiKey();

    String getAppPublicLicenseKey();

    boolean isAppInStore();

    String getSupportEmail();
}

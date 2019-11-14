package org.stepic.droid.configuration;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.remote.auth.model.TokenType;

public interface Config {

    String getOAuthClientId(@NotNull TokenType type);

    String getBaseUrl();

    String getOAuthClientSecret(TokenType type);

    String getGrantType(TokenType type);

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

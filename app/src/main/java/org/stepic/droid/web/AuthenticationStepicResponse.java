package org.stepic.droid.web;

import java.util.Locale;

public class AuthenticationStepicResponse {
    private String refresh_token;
    private long expires_in;
    private String scope;
    private String access_token;
    private String token_type;
    private String error;
    private String error_description;


    public String getRefresh_token() {
        return refresh_token;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "access_token=%s; access_type=%s; expires_in=%d; scope=%s; refresh_token=%s; error = %s; error_description = %s", access_token, token_type, expires_in, scope, refresh_token, error, error_description);
    }

    public boolean isSuccess() {
        return (refresh_token != null && access_token != null && expires_in > 0);
    }

}

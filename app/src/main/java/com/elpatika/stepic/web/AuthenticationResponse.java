package com.elpatika.stepic.web;

public class AuthenticationResponse implements IResponse {
    private String refresh_token;
    private long expires_in;
    private String scope;
    private String access_token;
    private String token_type;
    private String error;
    private String error_description;


    @Override
    public String toString() {
        return String.format("access_token=%s; access_type=%s; "
                + "expires_in=%d; scope=%s", access_token, token_type, expires_in, scope);
    }
}

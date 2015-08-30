package com.elpatika.stepic.concurrency;

import android.content.Context;

import com.elpatika.stepic.core.IShell;
import com.elpatika.stepic.web.AuthenticationResponse;
import com.elpatika.stepic.web.IApi;
import com.google.inject.Inject;

public class LoginTask extends StepicTask<AuthenticationResponse> {
    String mLogin;
    String mPassword;

    @Inject
    IShell mShell;


    public LoginTask(Context context, String login, String password) {
        super(context);
        mLogin = login;
        mPassword = password;
    }


    @Override
    public AuthenticationResponse call() throws Exception {
        try {
            if(mLogin!=null) {
                return getAuthResponse(context, mLogin, mPassword);
            }
        } catch(Exception ex) {
            handle(ex);
        }
        return null;
    }


    public  AuthenticationResponse getAuthResponse(Context context, String username, String password) throws Exception {
        IApi api = mShell.getApi();
        AuthenticationResponse response = (AuthenticationResponse) api.authWithLoginPassword(username, password);

        //todo: if isSuccess(), then get profile and save
        return response;
    }
}

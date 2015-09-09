package com.elpatika.stepic.concurrency;

import android.content.Context;

import com.elpatika.stepic.web.AuthenticationStepicResponse;
import com.elpatika.stepic.web.IApi;

public class LoginTask extends StepicTask<AuthenticationStepicResponse> {
    String mLogin;
    String mPassword;

    public LoginTask(Context context, String login, String password) {
        super(context);
        mLogin = login;
        mPassword = password;
    }


    @Override
    public AuthenticationStepicResponse call() throws Exception {
        try {
            if(mLogin!=null) {
                return getAuthResponse(context, mLogin, mPassword);
            }
        } catch(Exception ex) {
            handle(ex);
        }
        return null;
    }


    public AuthenticationStepicResponse getAuthResponse(Context context, String username, String password) throws Exception {
        IApi api = mShell.getApi();
        AuthenticationStepicResponse response = (AuthenticationStepicResponse) api.authWithLoginPassword(username, password);

        //todo: if isSuccess(), then get profile and save
        return response;
    }
}

package com.elpatika.stepic.concurrency;

import android.content.Context;

import com.elpatika.stepic.base.MainApplication;
import com.elpatika.stepic.core.IShell;
import com.elpatika.stepic.web.AuthenticationStepicResponse;
import com.elpatika.stepic.web.IApi;

import javax.inject.Inject;

public class LoginTask extends StepicTask<Void, Void, AuthenticationStepicResponse> {
    String mLogin;
    String mPassword;
    @Inject
    IShell mShell;

    public LoginTask(Context context, String login, String password) {
        super(context);
        MainApplication.component(mContext).inject(this);

        mLogin = login;
        mPassword = password;
    }

    @Override
    protected AuthenticationStepicResponse doInBackgroundBody(Void... params) throws Exception {
        if (mLogin != null) {
            AuthenticationStepicResponse response = getAuthResponse(mLogin, mPassword);
            if (response == null) throw new Exception("Something Wrong"); //todo: goto resources, classify: invalid login/password or connection problems?
            return response;
        } else {
            throw new Exception("Login is null");
        }
    }

    private AuthenticationStepicResponse getAuthResponse(String username, String password) throws Exception {
        IApi api = mShell.getApi();
        return (AuthenticationStepicResponse) api.authWithLoginPassword(username, password);
    }
}

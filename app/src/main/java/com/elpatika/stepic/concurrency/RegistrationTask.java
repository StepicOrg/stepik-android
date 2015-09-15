package com.elpatika.stepic.concurrency;

import android.content.Context;

import com.elpatika.stepic.base.MainApplication;
import com.elpatika.stepic.core.IShell;
import com.elpatika.stepic.web.IApi;
import com.elpatika.stepic.web.SignUpResponse;

import javax.inject.Inject;

public class RegistrationTask extends StepicTask<Void, Void, SignUpResponse> {

    private final String mFirstName;
    private final String mLastName;
    private final String mEmail;
    private final String mPassword;

    @Inject
    IShell mShell;

    public RegistrationTask(Context context, String mFirstName, String mLastName, String mEmail, String mPassword) {
        super(context);
        MainApplication.component(mContext).inject(this);

        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mEmail = mEmail;
        this.mPassword = mPassword;
    }


    @Override
    protected SignUpResponse doInBackgroundBody(Void... params) throws Exception {
        IApi api = mShell.getApi();
        return  (SignUpResponse) api.signUp(mFirstName, mLastName, mEmail, mPassword);
    }
}

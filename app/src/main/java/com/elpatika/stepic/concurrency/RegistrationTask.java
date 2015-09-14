package com.elpatika.stepic.concurrency;

import android.content.Context;

import com.elpatika.stepic.web.IApi;
import com.elpatika.stepic.web.SignUpResponse;

public class RegistrationTask extends StepicTask<Void, Void, SignUpResponse> {

    private final String mFirstName;
    private final String mLastName;
    private final String mEmail;
    private final String mPassword;

    public RegistrationTask(Context context, String mFirstName, String mLastName, String mEmail, String mPassword) {
        super(context);
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mEmail = mEmail;
        this.mPassword = mPassword;
    }

    @Override
    protected SignUpResponse doInBackground(Void... params) {
        IApi api = mShell.getApi();
        return  (SignUpResponse) api.signUp(mFirstName, mLastName, mEmail, mPassword);

    }
}

package org.stepic.droid.concurrency;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.exceptions.NullProfileException;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Profile;
import org.stepic.droid.web.IApi;

import java.util.List;

import javax.inject.Inject;

public class LoadingProfileInformation extends StepicTask<Void, Void, Profile> {

    @Inject
    IShell mShell;


    public LoadingProfileInformation(Context context) {
        super(context);
        MainApplication.component(mContext).inject(this);

    }

    @Override
    protected Profile doInBackgroundBody(Void... params) throws Exception {
        IApi api = mShell.getApi();
        Profile profile = api.getUserProfile();
        if (profile == null)
            throw new NullProfileException();
        return profile;
    }
}

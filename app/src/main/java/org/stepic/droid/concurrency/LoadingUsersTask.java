package org.stepic.droid.concurrency;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.User;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.IApi;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

public class LoadingUsersTask extends StepicTask<Void, Void, List<User>> {

    @Inject
    IShell mShell;

    private long[] mIdsOfUsers;

    public LoadingUsersTask(Context context, long[] idsOfUsers) {
        super(context);
        MainApplication.component(mContext).inject(this);

        mIdsOfUsers = idsOfUsers;
    }

    @Override
    protected List<User> doInBackgroundBody(Void... params) {
        IApi api = mShell.getApi();
        List<User> userList = null;
        userList = api.getUsers(mIdsOfUsers);
        return userList;

    }

}

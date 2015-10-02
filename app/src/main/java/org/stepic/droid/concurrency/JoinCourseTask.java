package org.stepic.droid.concurrency;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.web.IApi;

import javax.inject.Inject;

public class JoinCourseTask extends StepicTask<Void, Void, Boolean> {

    @Inject
    IShell mShell;

    private Course mCourse;

    public JoinCourseTask(Context context, @NotNull Course course) {
        super(context);
        MainApplication.component(mContext).inject(this);

        mCourse = course;
    }

    @Override
    protected Boolean doInBackgroundBody(Void... params) throws Exception {
        IApi api = mShell.getApi();
        return api.tryJoinCourse(mCourse);
    }
}

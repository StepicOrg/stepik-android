package org.stepic.droid.concurrency;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.web.IApi;

import java.util.List;

import javax.inject.Inject;

public class LoadingCoursesTask extends StepicTask <Void, Void, List<Course>> {

    @Inject
    IShell mShell;

    public LoadingCoursesTask(Context context) {
        super(context);
        MainApplication.component(mContext).inject(this);
    }

    @Override
    protected List<Course> doInBackgroundBody(Void... params) throws Exception {
        IApi api = mShell.getApi();
        return api.getEnrolledCourses();
    }
}

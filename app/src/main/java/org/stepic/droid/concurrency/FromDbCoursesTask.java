package org.stepic.droid.concurrency;

import com.squareup.otto.Bus;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.events.courses.FinishingGetCoursesFromDbEvent;
import org.stepic.droid.events.courses.StartingGetCoursesFromDbEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import java.util.List;

import javax.inject.Inject;

public class FromDbCoursesTask extends StepicTask<Void, Void, List<Course>> {

    @Inject
    IShell mShell;

    @Inject
    Bus bus;

    private DbOperationsCourses.Table mCourseType;

    public FromDbCoursesTask(@NotNull DbOperationsCourses.Table courseType) {
        super(MainApplication.getAppContext());

        MainApplication.component(mContext).inject(this);

        mCourseType = courseType;
    }

    @Override
    protected List<Course> doInBackgroundBody(Void... params) throws Exception {
        DbOperationsCourses dbOperationsCourses = mShell.getDbOperationsCourses(mCourseType);
        dbOperationsCourses.open();
        List<Course> fromCache = null;
        try {
            fromCache = dbOperationsCourses.getAllCourses();
        } finally {
            dbOperationsCourses.close();
        }
        return fromCache;
    }

    @Override
    protected void onPreExecute() {
        bus.post(new StartingGetCoursesFromDbEvent(mCourseType));
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<List<Course>> listAsyncResultWrapper) {
        super.onPostExecute(listAsyncResultWrapper);
        bus.post(new FinishingGetCoursesFromDbEvent(mCourseType));
    }
}

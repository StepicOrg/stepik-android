package org.stepic.droid.concurrency;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import java.util.List;

import javax.inject.Inject;

public class DbCoursesTask extends StepicTask<Void, Void, List<Course>> {

    @Inject
    IShell mShell;

    private DbOperationsCourses.Table mCourseType;

    public DbCoursesTask(@NotNull DbOperationsCourses.Table courseType) {
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
}

package org.stepic.droid.concurrency;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import javax.inject.Inject;

public class UpdateCourseTask extends StepicTask<Void, Void, Void> {
    @Inject
    IShell mShell;

    private final DbOperationsCourses.Table mCourseType;
    private Course mCourse;

    public UpdateCourseTask(DbOperationsCourses.Table mCourseType, Course course) {
        super(MainApplication.getAppContext());
        this.mCourseType = mCourseType;
        mCourse = course;
        MainApplication.component().inject(this);
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        DbOperationsCourses dbOperationsCourses = mShell.getDbOperationsCourses(mCourseType);
        dbOperationsCourses.open();
        try {
            dbOperationsCourses.deleteCourse(mCourse);
            dbOperationsCourses.addCourse(mCourse);
        } finally {
            dbOperationsCourses.close();
        }
        return null;
    }
}

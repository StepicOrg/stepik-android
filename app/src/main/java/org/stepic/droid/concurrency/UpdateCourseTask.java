package org.stepic.droid.concurrency;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseManager;

import javax.inject.Inject;

public class UpdateCourseTask extends StepicTask<Void, Void, Void> {
    @Inject
    IShell mShell;

    @Inject
    DatabaseManager mDatabaseManager;

    private final DatabaseManager.Table mCourseType;
    private Course mCourse;

    public UpdateCourseTask(DatabaseManager.Table mCourseType, Course course) {
        super(MainApplication.getAppContext());
        this.mCourseType = mCourseType;
        mCourse = course;
        MainApplication.component().inject(this);
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        mDatabaseManager.addCourse(mCourse, mCourseType);
        return null;
    }
}

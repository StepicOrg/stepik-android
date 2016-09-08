package org.stepic.droid.concurrency.tasks;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.store.operations.Table;

import javax.inject.Inject;

public class UpdateCourseTask extends StepicTask<Void, Void, Void> {
    @Inject
    IShell mShell;

    @Inject
    DatabaseFacade mDatabaseFacade;

    private final Table mCourseType;
    private Course mCourse;

    public UpdateCourseTask(Table mCourseType, Course course) {
        super(MainApplication.getAppContext());
        this.mCourseType = mCourseType;
        mCourse = course;
        MainApplication.component().inject(this);
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        //it is hack how to right update info without is_featured knowledge
        if (mCourseType == Table.enrolled ||
                (mCourseType == Table.featured && mDatabaseFacade.getCourseById(mCourse.getCourseId(), Table.featured) != null)) {
            mDatabaseFacade.addCourse(mCourse, mCourseType);
        }
        return null;
    }
}

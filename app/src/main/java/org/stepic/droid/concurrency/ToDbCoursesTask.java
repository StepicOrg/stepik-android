package org.stepic.droid.concurrency;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;

import java.util.List;

import javax.inject.Inject;

public class ToDbCoursesTask extends StepicTask<Void, Void, Void> {

    @Inject
    IShell mShell;

    private List<Course> mCourses;
    private DbOperationsCourses.Table mCourseType;

    public ToDbCoursesTask(List<Course> courses, DbOperationsCourses.Table type) {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);

        //courses now is not thread safe

        mCourseType = type;
        mCourses = courses;
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        DbOperationsCourses dbOperationsCourses = mShell.getDbOperationsCourses(mCourseType);
        dbOperationsCourses.open();
        try {
            for (Course courseItem : mCourses) {
                if (!dbOperationsCourses.isCourseInDB(courseItem))
                    dbOperationsCourses.addCourse(courseItem);
            }
        } finally {
            dbOperationsCourses.close();
        }
        return null;

    }
}

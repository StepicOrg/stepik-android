package org.stepic.droid.concurrency;

import android.content.Context;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.exceptions.NullCourseListException;
import org.stepic.droid.model.Course;
import org.stepic.droid.web.IApi;

import java.util.List;

import javax.inject.Inject;

public class LoadingCoursesTask extends StepicTask <Void, Void, List<Course>> {

    @Inject
    IShell mShell;

    private CourseType mCourseType;

    public enum CourseType {
        enrolled, featured
    }

    public LoadingCoursesTask(Context context, CourseType courseType) {
        super(context);
        MainApplication.component(mContext).inject(this);

        mCourseType = courseType;
    }

    @Override
    protected List<Course> doInBackgroundBody(Void... params) throws Exception {
        Thread.sleep(3000); //todo: delete fake latency for debug
        IApi api = mShell.getApi();
        List<Course> courseList = null;
        switch (mCourseType) {
            case enrolled:
                courseList = api.getEnrolledCourses();
                break;
            case featured:
                courseList = api.getFeaturedCourses();
                break;
        }

        if (courseList == null) throw new NullCourseListException();
        return courseList;
    }
}

package org.stepic.droid.concurrency;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.web.IApi;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

public class LoadingCoursesTask extends StepicTask<Void, Void, List<Course>> {

    @Inject
    IShell mShell;

    private CourseType mCourseType;

    public enum CourseType {
        enrolled, featured
    }

    public LoadingCoursesTask(CourseType courseType) {
        super(MainApplication.getAppContext());
        MainApplication.component(mContext).inject(this);

        mCourseType = courseType;
    }

    @Override
    protected List<Course> doInBackgroundBody(Void... params) throws Exception {
        IApi api = mShell.getApi();
        List<Course> courseList = null;
        try {
            switch (mCourseType) {
                case enrolled:
                    courseList = api.getEnrolledCourses();
                    break;
                case featured:
                    courseList = api.getFeaturedCourses();
                    break;
            }
        } finally {

            if (courseList != null) {
                List<Course> cachedCourses = getCachedCourses();

                DbOperationsCourses dbOperationCourses = mShell.getDbOperationsCourses(getDbType(mCourseType));

                try {
                    dbOperationCourses.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    for (Course courseItem : cachedCourses) {
                        if (!courseList.contains(courseItem)) {
                            dbOperationCourses.deleteCourse(courseItem);//remove outdated courses from cache
                            courseList.remove(courseItem);
                        }
                    }

                    for (Course newCourse : courseList) {
                        if (!dbOperationCourses.isCourseInDB(newCourse)) {
                            dbOperationCourses.addCourse(newCourse);//add new to persistent cache
                        }
                    }
                } finally {
                    dbOperationCourses.close();
                }
                //all courses are cached now
            }

            courseList = getCachedCourses(); //get from cache;
            return courseList;
        }

    }

    private DbOperationsCourses.Table getDbType(LoadingCoursesTask.CourseType type) {
        DbOperationsCourses.Table dbType = null;
        switch (type) {
            case enrolled:
                dbType = DbOperationsCourses.Table.enrolled;
                break;
            case featured:
                dbType = DbOperationsCourses.Table.featured;
        }
        return dbType;
    }

    private List<Course> getCachedCourses() {
        DbOperationsCourses dbOperationCourses = mShell.getDbOperationsCourses(getDbType(mCourseType));

        try {
            dbOperationCourses.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Course> cachedCourses = dbOperationCourses.getAllCourses();
        dbOperationCourses.close();

        return cachedCourses;
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<List<Course>> listAsyncResultWrapper) {
        super.onPostExecute(listAsyncResultWrapper);
    }
}

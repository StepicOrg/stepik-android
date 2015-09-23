package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.concurrency.AsyncResultWrapper;
import org.stepic.droid.concurrency.LoadingCoursesTask;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DbOperationsCourses;
import org.stepic.droid.view.adapters.MyCoursesAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class CoursesFragmentBase extends StepicBaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Bind(R.id.swipe_refresh_layout_mycourses)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_of_courses)
    ListView mListOfCourses;

    protected LoadingCoursesTask mLoadingCoursesTask;
    protected List<Course> mCourses;
    protected MyCoursesAdapter mCoursesAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        if (mCourses == null) mCourses = new ArrayList<>();
        mCoursesAdapter = new MyCoursesAdapter(getContext(), mCourses);
        mListOfCourses.setAdapter(mCoursesAdapter);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });
    }

    public final LoadingCoursesTask initCoursesLoadingTask(final LoadingCoursesTask.CourseType type) {
        LoadingCoursesTask task = new LoadingCoursesTask(getActivity(), type) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);

                DbOperationsCourses dbOperationCourses = mShell.getDbOperationsCourses(getDbType(type));
                try {
                    dbOperationCourses.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                List<Course> cachedCourses = dbOperationCourses.getAllCourses();

                for (Course courseItem : cachedCourses) {
                    if (!courses.contains(courseItem)) {
                        dbOperationCourses.deleteCourse(courseItem);//remove outdated courses from cache
                        courses.remove(courseItem);
                    }
                }

                for (Course newCourse : courses) {
                    if (!dbOperationCourses.isCourseInDB(newCourse)) {
                        dbOperationCourses.addCourse(newCourse);//add new to persistent cache
                    }
                }
                dbOperationCourses.close();
                //all courses are cached now

                showCachedCourses(getDbType(type));
            }

            @Override
            protected void onException(Throwable exception) {
                super.onException(exception);

                showCachedCourses(getDbType(type));
            }

            @Override
            protected void onPostExecute(AsyncResultWrapper<List<Course>> listAsyncResultWrapper) {
                super.onPostExecute(listAsyncResultWrapper);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        return task;
    }


    protected void showCachedCourses(DbOperationsCourses.Table type) {
        DbOperationsCourses dbOperationCourses = mShell.getDbOperationsCourses(type);
        try {
            dbOperationCourses.open();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        List<Course> cachedCourses = dbOperationCourses.getAllCourses();
        dbOperationCourses.close();

        mCourses.clear();
        mCourses.addAll(cachedCourses);
        mCoursesAdapter.notifyDataSetChanged();
    }

    @Override
    public abstract void onRefresh();


    private DbOperationsCourses.Table getDbType (LoadingCoursesTask.CourseType type) {
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

}

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

public class MyCoursesFragment extends StepicBaseFragment implements SwipeRefreshLayout.OnRefreshListener {


    @Bind(R.id.swipe_refresh_layout_mycourses)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_of_courses)
    ListView mListOfCourses;

    private List<Course> mCourses;
    private MyCoursesAdapter mCoursesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.stepic_brand_primary,
                R.color.orange,
                R.color.blue);

        if (mCourses == null) mCourses = new ArrayList<>();
        mCoursesAdapter = new MyCoursesAdapter(getContext(), mCourses);
        mListOfCourses.setAdapter(mCoursesAdapter);


    }

    @Override
    public void onRefresh() {
        LoadingCoursesTask task = new LoadingCoursesTask(getActivity(), LoadingCoursesTask.CourseType.enrolled) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);
                mCourses.clear();

                DbOperationsCourses dbOperationCourses = mShell.getDbOperationsCourses();
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

                courses = dbOperationCourses.getAllCourses();
                dbOperationCourses.close();

                mCourses.addAll(courses);
                mCoursesAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onException(Throwable exception) {
                super.onException(exception);

                DbOperationsCourses dbOperationCourses = mShell.getDbOperationsCourses();
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
            protected void onPostExecute(AsyncResultWrapper<List<Course>> listAsyncResultWrapper) {
                super.onPostExecute(listAsyncResultWrapper);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };
        task.execute();
    }
}

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
import org.stepic.droid.view.adapters.MyCoursesAdapter;

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

        if (mCourses == null) mCourses = new ArrayList<Course>();
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
                if (courses == null) return;
                mCourses.addAll(courses);
                mCoursesAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onException(Exception exception) {
                super.onException(exception);
                int doNothing = 0;
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

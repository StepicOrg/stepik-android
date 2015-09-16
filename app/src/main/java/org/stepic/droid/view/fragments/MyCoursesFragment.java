package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.stepic.droid.R;
import org.stepic.droid.base.StepicBaseFragment;
import org.stepic.droid.concurrency.LoadingCoursesTask;
import org.stepic.droid.model.Course;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyCoursesFragment extends StepicBaseFragment {

    @Bind(R.id.login_spinner)
    ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_courses,container,false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoadingCoursesTask task = new LoadingCoursesTask(getActivity()) {
            @Override
            protected void onSuccess(List<Course> courses) {
                super.onSuccess(courses);
                int doNothing = 0;
            }

            @Override
            protected void onException(Exception exception) {
                super.onException(exception);
                int doNothing = 0;
            }
        };
        task.setProgressBar(mProgressBar);
        task.execute();
    }
}

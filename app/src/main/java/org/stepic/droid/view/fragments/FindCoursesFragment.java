package org.stepic.droid.view.fragments;

import android.os.AsyncTask;
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

public class FindCoursesFragment extends CoursesFragmentBase {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTypeOfCourse = DbOperationsCourses.Table.featured;
    }
}

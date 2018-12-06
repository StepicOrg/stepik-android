package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CourseProperty;
import org.stepic.droid.ui.adapters.CoursePropertyAdapter;
import org.stepic.droid.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class CourseDetailFragment extends FragmentBase {
    @BindView(R.id.list_of_course_property)
    ListView coursePropertyListView;

    private List<CourseProperty> coursePropertyList;
    private Course course;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_detailed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //VIEW:
        coursePropertyList = new ArrayList<>();

        coursePropertyListView.setAdapter(new CoursePropertyAdapter(getActivity(), coursePropertyList));
        hideSoftKeypad();

        //COURSE RELATED IN ON START
    }

    public void finish() {
        Intent intent = new Intent();
        if (course != null) {
            intent.putExtra(AppConstants.COURSE_ID_KEY, course);
            intent.putExtra(AppConstants.ENROLLMENT_KEY, course.getEnrollment());
            getActivity().setResult(Activity.RESULT_OK, intent);
        }
        getActivity().finish();
    }
}

package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.base.CoursesDatabaseFragmentBase;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.ui.listeners.OnRootTouchedListener;
import org.stepic.droid.ui.util.ToolbarHelperKt;

public class FindCoursesFragment extends CoursesDatabaseFragmentBase {

    public static FindCoursesFragment newInstance() {
        return new FindCoursesFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onViewCreated(view, savedInstanceState);
        ToolbarHelperKt.initCenteredToolbar(this, getTitle(), false);
        rootView.setParentTouchEvent(new OnRootTouchedListener() {
            @Override
            public void makeBeforeChildren() {
                collapseAndHide(true);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (listOfCoursesView != null) {
            listOfCoursesView.setOnFocusChangeListener(null);
        }
        super.onDestroyView();
    }

    private void collapseAndHide(boolean rootHandle) {
    }

    @Override
    protected Table getCourseType() {
        return Table.featured;
    }

    protected String getTitle() {
        return getString(R.string.catalog_title);
    }
}

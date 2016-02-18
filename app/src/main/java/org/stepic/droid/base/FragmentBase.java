package org.stepic.droid.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.squareup.leakcanary.RefWatcher;
import com.squareup.otto.Bus;

import org.stepic.droid.core.ILessonSessionManager;
import org.stepic.droid.core.ILocalProgressManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.ISearchResolver;
import org.stepic.droid.util.resolvers.IVideoResolver;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class FragmentBase extends Fragment {

    protected String TAG = "StepicFragment";

    @Inject
    public ILessonSessionManager mLessonManager;

    @Inject
    public ISearchResolver mSearchResolver;

    @Inject
    public DatabaseFacade mDatabaseFacade;

    @Inject
    public Bus bus;

    @Inject
    public IShell mShell;

    @Inject
    public ILocalProgressManager mLocalProgressManager;


    @Inject
    public IDownloadManager mDownloadManager;


    @Inject
    public IVideoResolver mVideoResolver;


    @Inject
    public SharedPreferenceHelper mSharedPreferenceHelper;

    @Inject
    public UserPreferences mUserPreferences;

    @Inject
    public CoursePropertyResolver mCoursePropertyResolver;


    public FragmentBase() {
        MainApplication.component(MainApplication.getAppContext()).inject(this);
    }

    protected void hideSoftKeypad() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) (getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MainApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

package org.stepic.droid.base;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.AudioFocusHelper;
import org.stepic.droid.core.ILessonSessionManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.core.LocalProgressManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.ICancelSniffer;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.ISearchResolver;
import org.stepic.droid.util.resolvers.IVideoResolver;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.Unbinder;

public class FragmentBase extends Fragment {

//    protected String TAG = "StepicFragment";

    protected Unbinder unbinder;

    @Inject
    protected ShareHelper shareHelper;

    @Inject
    protected Analytic analytic;

    @Inject
    public ThreadPoolExecutor mThreadPoolExecutor;

    @Inject
    public ILessonSessionManager mLessonManager;

    @Inject
    public ISearchResolver mSearchResolver;

    @Inject
    public DatabaseFacade mDatabaseFacade;

    @Inject
    public Bus bus;

    @Inject
    public IConfig config;

    @Inject
    public IShell mShell;

    @Inject
    public LocalProgressManager mLocalProgressManager;


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

    @Inject
    public IMainHandler mMainHandler;

    @Inject
    public AudioFocusHelper mAudioFocusHelper;

    @Inject
    public DownloadManager mSystemDownloadManager;

    @Inject
    public ICancelSniffer cancelSniffer;

    public FragmentBase() {
        injectComponent();
    }

    protected void injectComponent() {
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
        if (unbinder != null) {
            //in Kotlin, for example, butter knife is not used
            unbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MainApplication.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

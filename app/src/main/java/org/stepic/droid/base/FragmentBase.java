package org.stepic.droid.base;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.AudioFocusHelper;
import org.stepic.droid.core.LocalProgressManager;
import org.stepic.droid.core.MyExoPhoneStateListener;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.fonts.FontsProvider;
import org.stepic.droid.notifications.LocalReminder;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.storage.CancelSniffer;
import org.stepic.droid.storage.IDownloadManager;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.text.TextResolver;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentBase extends Fragment {

    private boolean viewHasBeenDestroyed = false;
    private Unbinder unbinder;

    @Inject
    protected LocalReminder localReminder;

    @Inject
    protected TextResolver textResolver;

    @Inject
    protected ShareHelper shareHelper;

    @Inject
    protected Analytic analytic;

    @Inject
    public ThreadPoolExecutor threadPoolExecutor;

    @Inject
    public DatabaseFacade databaseFacade;

    @Inject
    protected FontsProvider fontsProvider;

    @Inject
    protected Config config;

    @Inject
    protected Api api;

    @Inject
    protected ScreenManager screenManager;

    @Inject
    protected LocalProgressManager localProgressManager;

    @Inject
    protected IDownloadManager downloadManager;

    @Inject
    protected SharedPreferenceHelper sharedPreferenceHelper;

    @Inject
    protected UserPreferences userPreferences;

    @Inject
    protected CoursePropertyResolver coursePropertyResolver;

    @Inject
    protected MainHandler mainHandler;

    @Inject
    protected AudioFocusHelper audioFocusHelper;

    @Inject
    protected DownloadManager systemDownloadManager;

    @Inject
    protected CancelSniffer cancelSniffer;

    @Inject
    protected MyExoPhoneStateListener exoPhoneListener;

    public FragmentBase() {

    }

    protected void injectComponent() {
        App.Companion.component().inject(this);
    }

    /**
     * optional method for releasing components
     * mirror of {@code injectComponent()}
     */
    protected void onReleaseComponent() {
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
        setRetainInstance(true);
        injectComponent();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            //in Kotlin, for example, butter knife is not used
            unbinder.unbind();
        }
        viewHasBeenDestroyed = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onReleaseComponent();
//        RefWatcher refWatcher = App.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        boolean shouldNotBeenDestroyed = viewHasBeenDestroyed && enter;
        viewHasBeenDestroyed = false;

        //do not animate fragment on rotation via setCustomAnimations
        return shouldNotBeenDestroyed ? new Animation() {
        } : super.onCreateAnimation(transit, enter, nextAnim);
    }
}
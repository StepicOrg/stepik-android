
package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.services.ViewPusher;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.view.activities.CourseDetailActivity;
import org.stepic.droid.view.activities.LaunchActivity;
import org.stepic.droid.view.activities.LoginActivity;
import org.stepic.droid.view.activities.MainFeedActivity;
import org.stepic.droid.view.activities.RegisterActivity;
import org.stepic.droid.view.activities.SectionActivity;
import org.stepic.droid.view.activities.SettingsActivity;
import org.stepic.droid.view.activities.StepsActivity;
import org.stepic.droid.view.activities.TextFeedbackActivity;
import org.stepic.droid.view.activities.UnitsActivity;
import org.stepic.droid.view.activities.VideoActivity;
import org.stepic.droid.view.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.web.ViewAssignment;
import org.videolan.libvlc.util.VLCUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScreenManager implements IScreenManager {
    private IConfig mConfig;
    private UserPreferences mUserPreferences;

    @Inject
    public ScreenManager(IConfig config, UserPreferences userPreferences) {
        this.mConfig = config;
        mUserPreferences = userPreferences;
    }

    @Override
    public void showLaunchScreen(Context context, boolean overrideAnimation) {
        YandexMetrica.reportEvent("Screen manager: show launch screen");
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.OVERRIDE_ANIMATION_FLAG, overrideAnimation);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }


    @Override
    public void showRegistration(Activity sourceActivity) {
        YandexMetrica.reportEvent("Screen manager: show registration");
        Intent launchIntent = new Intent(sourceActivity, RegisterActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    @Override
    public void showLogin(Context sourceActivity) {
        YandexMetrica.reportEvent("Screen manager: show login");
        Intent loginIntent = new Intent(sourceActivity, LoginActivity.class);
        sourceActivity.startActivity(loginIntent);
    }

    @Override
    public void showMainFeed(Context sourceActivity) {
        YandexMetrica.reportEvent("Screen manager: show main feed");
        //todo onFinish all activities which exist for login (launch, splash, etc).
        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        /*
        Using CLEAR_TOP flag, causes the activity to be re-created every time.
        This reloads the list of courses. We don't want that.
        Using REORDER_TO_FRONT solves this problem
         */
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        sourceActivity.startActivity(intent);

        // let login screens be ended
//        Intent loginIntent = new Intent();
//        loginIntent.setAction(AppConstants.USER_LOG_IN);
//        sourceActivity.sendBroadcast(loginIntent);

    }

    @Override
    public void showCourseDescription(Fragment sourceFragment, @NotNull Course course) {
        YandexMetrica.reportEvent("Screen manager: show course description");
        Intent intent = new Intent(sourceFragment.getActivity(), CourseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        intent.putExtras(bundle);
        sourceFragment.startActivityForResult(intent, AppConstants.REQUEST_CODE_DETAIL);
    }

    @Override
    public void showCourseDescription(Activity sourceActivity, @NotNull Course course) {
        YandexMetrica.reportEvent("Screen manager: show course description");
        Intent intent = new Intent(sourceActivity, CourseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showTextFeedback(Activity sourceActivity) {
        Intent launchIntent = new Intent(sourceActivity, TextFeedbackActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    @Override
    public void showStoreWithApp(@NotNull Activity sourceActivity) {
        final String appPackageName = sourceActivity.getPackageName();
        try {
            sourceActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            sourceActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Override
    public void showDownload() {
        Context context = MainApplication.getAppContext();
        showDownload(context);
    }

    @Override
    public void showDownload(Context context) {
        Intent intent = new Intent(context, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        int index = MainFeedActivity.getDownloadFragmentIndex();
        bundle.putInt(MainFeedActivity.KEY_CURRENT_INDEX, index);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void showVideo(Activity sourceActivity, String videoPath) {
        YandexMetrica.reportEvent("video is tried to show");
        boolean isOpenExternal = mUserPreferences.isOpenInExternal();
        if (isOpenExternal) {
            YandexMetrica.reportEvent("video open external");
        } else {
            YandexMetrica.reportEvent("video open native");
        }

        boolean isCompatible = VLCUtil.hasCompatibleCPU(MainApplication.getAppContext());
        if (!isCompatible) {
            YandexMetrica.reportEvent("video is not compatible");
        }


        if (isCompatible && !isOpenExternal) {
            Intent intent = new Intent(MainApplication.getAppContext(), VideoActivity.class);
            intent.putExtra(VideoActivity.Companion.getVideoPathKey(), videoPath);
            sourceActivity.startActivity(intent);
        } else {
            Uri videoUri = Uri.parse(videoPath);
            Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
            intent.setDataAndType(videoUri, "video/*");
            try {
                sourceActivity.startActivity(intent);
            } catch (Exception ex) {
                YandexMetrica.reportError("NotPlayer", ex);
                Toast.makeText(sourceActivity, R.string.not_video_player_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void showSettings(Activity sourceActivity) {
        Intent intent = new Intent(sourceActivity, SettingsActivity.class);
        sourceActivity.startActivity(intent);
        sourceActivity.overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
    }

    @Override
    public void showSections(Context sourceActivity, @NotNull Course course) {
        YandexMetrica.reportEvent("Screen manager: show section", JsonHelper.toJson(course));
        Intent intent = new Intent(sourceActivity, SectionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showUnitsForSection(Context sourceActivity, @NotNull Section section) {
        YandexMetrica.reportEvent("Screen manager: show units-lessons screen", JsonHelper.toJson(section));
        Intent intent = new Intent(sourceActivity, UnitsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_SECTION_BUNDLE, section);
        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showSteps(Context sourceActivity, Unit unit, Lesson lesson) {
        YandexMetrica.reportEvent("Screen manager: show steps of lesson", JsonHelper.toJson(lesson));
        Intent intent = new Intent(sourceActivity, StepsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_UNIT_BUNDLE, unit);
        bundle.putSerializable(AppConstants.KEY_LESSON_BUNDLE, lesson);

        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void openStepInWeb(Context context, Step step) {
        YandexMetrica.reportEvent("Screen manager: open Step in Web", JsonHelper.toJson(step));
        String url = mConfig.getBaseUrl() + "/lesson/" + step.getLesson() + "/step/" + step.getPosition();
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void openRemindPassword(AppCompatActivity context) {
        YandexMetrica.reportEvent("Screen manager: remind password");
        android.support.v4.app.DialogFragment dialogFragment = RemindPasswordDialogFragment.newInstance();
        dialogFragment.show(context.getSupportFragmentManager(), null);
    }

    @Override
    public void pushToViewedQueue(ViewAssignment viewAssignmentWrapper) {

        Intent loadIntent = new Intent(MainApplication.getAppContext(), ViewPusher.class);

        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, viewAssignmentWrapper.getStep());
        loadIntent.putExtra(AppConstants.KEY_ASSIGNMENT_BUNDLE, viewAssignmentWrapper.getAssignment());
        MainApplication.getAppContext().startService(loadIntent);
    }

}

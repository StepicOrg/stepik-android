
package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.view.activities.LaunchActivity;
import org.stepic.droid.view.activities.LoginActivity;
import org.stepic.droid.view.activities.MainFeedActivity;
import org.stepic.droid.view.activities.NotEnrolledCourseDetailActivity;
import org.stepic.droid.view.activities.RegisterActivity;
import org.stepic.droid.view.activities.SectionActivity;
import org.stepic.droid.view.activities.StepsActivity;
import org.stepic.droid.view.activities.UnitsActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScreenManager implements IScreenManager {
    private IConfig mConfig;

    @Inject
    public ScreenManager(IConfig config) {
        this.mConfig = config;
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
        //todo finish all activities which exist for login (launch, splash, etc).
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
    public void showCourseDescription(Context sourceActivity, @NotNull Course course) {
        YandexMetrica.reportEvent("Screen manager: show course description");
        Intent intent = new Intent(sourceActivity, NotEnrolledCourseDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
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
    public void openSignUpInWeb(Context context) {
        YandexMetrica.reportEvent("Screen manager: open signup in Web");
        String url = mConfig.getBaseUrl() + "/accounts/signup/";
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        context.startActivity(intent);
    }

}

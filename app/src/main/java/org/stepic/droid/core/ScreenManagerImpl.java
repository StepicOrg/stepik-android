
package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.services.ViewPusher;
import org.stepic.droid.store.operations.Table;
import org.stepic.droid.ui.activities.AboutAppActivity;
import org.stepic.droid.ui.activities.CommentsActivity;
import org.stepic.droid.ui.activities.CourseDetailActivity;
import org.stepic.droid.ui.activities.FeedbackActivity;
import org.stepic.droid.ui.activities.FilterActivity;
import org.stepic.droid.ui.activities.LaunchActivity;
import org.stepic.droid.ui.activities.LoginActivity;
import org.stepic.droid.ui.activities.MainFeedActivity;
import org.stepic.droid.ui.activities.NewCommentActivity;
import org.stepic.droid.ui.activities.PhotoViewActivity;
import org.stepic.droid.ui.activities.ProfileActivity;
import org.stepic.droid.ui.activities.RegisterActivity;
import org.stepic.droid.ui.activities.SectionActivity;
import org.stepic.droid.ui.activities.SettingsActivity;
import org.stepic.droid.ui.activities.SplashActivity;
import org.stepic.droid.ui.activities.StepsActivity;
import org.stepic.droid.ui.activities.StoreManagementActivity;
import org.stepic.droid.ui.activities.TextFeedbackActivity;
import org.stepic.droid.ui.activities.UnitsActivity;
import org.stepic.droid.ui.activities.VideoActivity;
import org.stepic.droid.ui.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.ui.fragments.SectionsFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.web.ViewAssignment;
import org.videolan.libvlc.util.VLCUtil;

import java.net.URLEncoder;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScreenManagerImpl implements ScreenManager {
    private final SharedPreferenceHelper sharedPreferences;
    private final IConfig config;
    private final UserPreferences userPreferences;
    private final Analytic analytic;

    @Inject
    public ScreenManagerImpl(IConfig config, UserPreferences userPreferences, Analytic analytic, SharedPreferenceHelper sharedPreferences) {
        this.config = config;
        this.userPreferences = userPreferences;
        this.analytic = analytic;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void showLaunchScreen(Activity activity) {
        showLaunchScreen(activity, false, 0);
    }

    @Override
    public void showLaunchScreen(FragmentActivity activity, @NotNull Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_LAUNCH);
        Intent launchIntent = new Intent(activity, LaunchActivity.class);
        launchIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, (Parcelable) course);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(launchIntent);
    }

    @Override
    public void openImage(Context context, String path) {
        analytic.reportEvent(Analytic.Interaction.USER_OPEN_IMAGE);
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(PhotoViewActivity.pathKey, path);
        context.startActivity(intent);
    }

    @Override
    public void showLaunchScreen(Activity activity, boolean fromMainFeed, int index) {
        analytic.reportEvent(Analytic.Screens.SHOW_LAUNCH);
        Intent launchIntent = new Intent(activity, LaunchActivity.class);
        if (fromMainFeed) {
            launchIntent.putExtra(AppConstants.FROM_MAIN_FEED_FLAG, true);
            launchIntent.putExtra(MainFeedActivity.KEY_CURRENT_INDEX, index);
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(launchIntent);
    }


    @Override
    public void showRegistration(Activity sourceActivity, @Nullable Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_REGISTRATION);
        Intent launchIntent = new Intent(sourceActivity, RegisterActivity.class);
        if (course != null) {
            launchIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, (Parcelable) course);
        }
        sourceActivity.startActivity(launchIntent);
    }

    @Override
    public void showLogin(Activity sourceActivity, @Nullable Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_LOGIN);
        Intent loginIntent = new Intent(sourceActivity, LoginActivity.class);
        if (course != null) {
            loginIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, (Parcelable) course);
        }
        sourceActivity.startActivity(loginIntent);
    }

    @Override
    public void showMainFeed(Context sourceActivity) {
        showMainFeed(sourceActivity, null);
    }

    @Override
    public void showMainFeed(Context sourceActivity, @Nullable Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_MAIN_FEED);
        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        if (course != null) {
            intent.putExtra(AppConstants.KEY_COURSE_BUNDLE, (Parcelable) course);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showMainFeed(Context sourceActivity, int indexOfMenu) {
        analytic.reportEvent(Analytic.Screens.SHOW_MAIN_FEED);
        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainFeedActivity.KEY_CURRENT_INDEX, indexOfMenu);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showCourseDescription(Fragment sourceFragment, @NotNull Course course) {
        Intent intent = getIntentForDescription(sourceFragment.getActivity(), course);
        sourceFragment.startActivityForResult(intent, AppConstants.REQUEST_CODE_DETAIL);
    }

    @Override
    public void showCourseDescription(Activity sourceActivity, @NotNull Course course) {
        Intent intent = getIntentForDescription(sourceActivity, course);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showCourseDescription(Activity sourceActivity, @NotNull Course course, boolean instaEnroll) {
        Intent intent = getIntentForDescription(sourceActivity, course);
        intent.putExtra(CourseDetailActivity.INSTA_ENROLL_KEY, instaEnroll);
        sourceActivity.startActivity(intent);
    }

    private Intent getIntentForDescription(Activity sourceActivity, @NotNull Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_COURSE_DESCRIPTION);
        Intent intent = new Intent(sourceActivity, CourseDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void showTextFeedback(Activity sourceActivity) {
        analytic.reportEvent(Analytic.Screens.SHOW_TEXT_FEEDBACK);
        Intent launchIntent = new Intent(sourceActivity, TextFeedbackActivity.class);
        sourceActivity.startActivity(launchIntent);
    }

    @Override
    public void showStoreWithApp(@NotNull Activity sourceActivity) {
        analytic.reportEvent(Analytic.Screens.OPEN_STORE);
        final String appPackageName = sourceActivity.getPackageName();
        try {
            sourceActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            sourceActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    @Override
    public Intent getCertificateIntent() {
        Context context = MainApplication.getAppContext();
        int index = MainFeedActivity.getCertificateFragmentIndex();
        Intent intent = new Intent(context, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(MainFeedActivity.KEY_CURRENT_INDEX, index);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void showCertificates() {
        Context context = MainApplication.getAppContext();
        int index = MainFeedActivity.getCertificateFragmentIndex();
        context.startActivity(getFromMainActivityIntent(context, index));
    }

    @Override
    public void showDownload() {
        Context context = MainApplication.getAppContext();
        showDownload(context);
    }

    @Override
    public void showDownload(Context context) {
        int index = MainFeedActivity.getDownloadFragmentIndex();
        context.startActivity(getFromMainActivityIntent(context, index));
    }

    @Override
    public void showFindCourses(Context context) {
        context.startActivity(getShowFindCoursesIntent(context));
    }

    @Override
    public Intent getShowFindCoursesIntent(Context context) {
        int index = MainFeedActivity.getFindCoursesIndex();
        return getFromMainActivityIntent(context, index);
    }

    private Intent getFromMainActivityIntent(Context context, int index) {
        Intent intent = new Intent(context, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(MainFeedActivity.KEY_CURRENT_INDEX, index);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void showVideo(Activity sourceActivity, String videoPath, long videoId) {
        analytic.reportEvent(Analytic.Screens.TRY_OPEN_VIDEO);
        boolean isOpenExternal = userPreferences.isOpenInExternal();
        if (isOpenExternal) {
            analytic.reportEvent(Analytic.Video.OPEN_EXTERNAL);
        } else {
            analytic.reportEvent(Analytic.Video.OPEN_NATIVE);
        }

        boolean isCompatible = VLCUtil.hasCompatibleCPU(MainApplication.getAppContext());
        if (!isCompatible) {
            analytic.reportEvent(Analytic.Video.NOT_COMPATIBLE);
        }

        if (isCompatible && !isOpenExternal) {
            Intent intent = new Intent(MainApplication.getAppContext(), VideoActivity.class);
            intent.putExtra(VideoActivity.Companion.getVideoPathKey(), videoPath);
            intent.putExtra(VideoActivity.Companion.getVideoIdKey(), videoId);
            sourceActivity.startActivity(intent);
        } else {
            Uri videoUri = Uri.parse(videoPath);
            String scheme = videoUri.getScheme();
            if (scheme == null) {
                videoUri = Uri.parse(AppConstants.FILE_SCHEME_PREFIX + videoPath);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
            intent.setDataAndType(videoUri, "video/*");
            try {
                sourceActivity.startActivity(intent);
            } catch (Exception ex) {
                analytic.reportError(Analytic.Error.NOT_PLAYER, ex);
                Toast.makeText(sourceActivity, R.string.not_video_player_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void showSettings(Activity sourceActivity) {
        analytic.reportEvent(Analytic.Screens.SHOW_SETTINGS);
        Intent intent = new Intent(sourceActivity, SettingsActivity.class);
        sourceActivity.startActivity(intent);
        sourceActivity.overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
    }

    @Override
    public void showStorageManagement(Activity activity) {
        analytic.reportEvent(Analytic.Screens.SHOW_STORAGE_MANAGEMENT);
        Intent intent = new Intent(activity, StoreManagementActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
    }


    @Override
    public Intent getOpenInWebIntent(String path) {
        if (!path.startsWith("https://") && !path.startsWith("http://")) {
            path = "http://" + path;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(path));
        return intent;
    }

    @Override
    public void openProfile(Activity activity) {
        analytic.reportEvent(Analytic.Profile.CLICK_OPEN_MY_PROFILE);
        final Intent intent = new Intent(activity, ProfileActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void openProfile(Activity activity, long userId) {
        final Intent intent = new Intent(activity, ProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(ProfileActivity.Companion.getOptionalUserIdKey(), userId);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    public void openFeedbackActivity(Activity activity) {
        final Intent intent = new Intent(activity, FeedbackActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public Intent getMyCoursesIntent(@NotNull Context context) {
        int index = MainFeedActivity.getMyCoursesIndex();
        return getFromMainActivityIntent(context, index);
    }

    @Nullable
    @Override
    public Intent getProfileIntent(@NotNull Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public void openSplash(Context context) {
        Intent launchIntent = new Intent(context, SplashActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }

    @Override
    public void openAboutActivity(Activity activity) {
        analytic.reportEvent(Analytic.Screens.USER_OPEN_ABOUT_APP);
        Intent intent = new Intent(activity, AboutAppActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
    }

    @Override
    public void openPrivacyPolicyWeb(Activity activity) {
        String privacyPolicyUrl = config.getPrivacyPolicyUrl();
        openInWeb(activity, privacyPolicyUrl);
    }

    @Override
    public void openTermsOfServiceWeb(Activity activity) {
        String termsOfServiceUrl = config.getTermsOfServiceUrl();
        openInWeb(activity, termsOfServiceUrl);
    }

    @Override
    public void continueCourse(Activity activity, long courseId, Section section, long lessonId, long unitId, long stepPosition) {
        String testStepPath = StringUtil.getUriForStepByIds(config.getBaseUrl(), lessonId, unitId, stepPosition);
        String testSectionPath = StringUtil.getUriForCourse(config.getBaseUrl(), courseId + "");

        TaskStackBuilder.create(activity)
                .addNextIntent(new Intent(activity, MainFeedActivity.class)
                        .setAction(AppConstants.INTERNAL_STEPIK_ACTION))
                .addNextIntent(new Intent(activity, SectionActivity.class)
                        .setAction(AppConstants.INTERNAL_STEPIK_ACTION)
                        .setData(Uri.parse(testSectionPath)))
                .addNextIntent(getIntentForUnits(activity, section)
                        .setAction(AppConstants.INTERNAL_STEPIK_ACTION))
                .addNextIntent(new Intent(activity, StepsActivity.class)
                        .setAction(AppConstants.INTERNAL_STEPIK_ACTION)
                        .setData(Uri.parse(testStepPath)))
                .startActivities();
    }

    @Override
    public void openInWeb(Activity context, String path) {
        analytic.reportEventWithIdName(Analytic.Screens.OPEN_LINK_IN_WEB, "0", path);
        final Intent intent = getOpenInWebIntent(path);
        context.startActivity(intent);
    }

    @Override
    public void addCertificateToLinkedIn(CertificateViewItem certificateViewItem) {
        StringBuilder sb = new StringBuilder();
        sb.append(AppConstants.LINKEDIN_ADD_URL);
        sb.append("_ed=");//linkedin id parameter
        sb.append(AppConstants.LINKEDIN_ED_ID);
        sb.append("&pfCertificationName="); // linkedin cert name
        sb.append(URLEncoder.encode(certificateViewItem.getTitle()));
        sb.append("&pfCertificationUrl=");//linkedin certificate url
        sb.append(certificateViewItem.getFullPath());

        String issueDate = certificateViewItem.getIssue_date();
        if (issueDate != null) {
            sb.append("&pfCertStartDate=");
            DateTime issueDateTime = new DateTime(issueDate);
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMM").withZone(DateTimeZone.getDefault()).withLocale(Locale.getDefault());
            String issueDateInLinkedinFormat = formatter.print(issueDateTime);
            sb.append(issueDateInLinkedinFormat);
        }


        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(sb.toString()));
        MainApplication.getAppContext().startActivity(intent);
    }

    @Override
    public void showFilterScreen(Fragment sourceFragment, int requestCode, Table courseType) {
        Intent intent = new Intent(sourceFragment.getContext(), FilterActivity.class);
        int code;
        if (courseType == Table.enrolled) {
            code = AppConstants.ENROLLED_FILTER;
        } else {
            code = AppConstants.FEATURED_FILTER;
        }
        intent.putExtra(FilterActivity.FILTER_TYPE_KEY, code);
        sourceFragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void showPdfInBrowserByGoogleDocs(Activity activity, String fullPath) {
        String googleDocsUrl = "https://docs.google.com/viewer?url=";
        openInWeb(activity, googleDocsUrl + fullPath);
    }

    @Override
    public void openComments(Context context, @Nullable String discussionProxyId, long stepId) {

        if (discussionProxyId == null) {
            analytic.reportEvent(Analytic.Screens.OPEN_COMMENT_NOT_AVAILABLE);
            Toast.makeText(context, R.string.comment_denied, Toast.LENGTH_SHORT).show();
        } else {
            analytic.reportEvent(Analytic.Screens.OPEN_COMMENT);
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putString(CommentsActivity.Companion.getKeyDiscussionProxyId(), discussionProxyId);
            bundle.putLong(CommentsActivity.Companion.getKeyStepId(), stepId);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    @Override
    public void openNewCommentForm(Activity sourceActivity, Long target, @Nullable Long parent) {
        if (sharedPreferences.getAuthResponseFromStore() != null) {
            analytic.reportEvent(Analytic.Screens.OPEN_WRITE_COMMENT);
            Intent intent = new Intent(sourceActivity, NewCommentActivity.class);
            Bundle bundle = new Bundle();
            if (parent != null) {
                bundle.putLong(NewCommentActivity.Companion.getKeyParent(), parent);
            }
            bundle.putLong(NewCommentActivity.Companion.getKeyTarget(), target);
            intent.putExtras(bundle);
            sourceActivity.startActivity(intent);
        } else {
            Toast.makeText(sourceActivity, R.string.anonymous_write_comment, Toast.LENGTH_SHORT).show();
        }
    }

    private Intent getSectionsIntent(Activity sourceActivity, @NotNull Course course) {
        Intent intent = new Intent(sourceActivity, SectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, course);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void showSections(Activity sourceActivity, @NotNull Course course) {
        analytic.reportEventWithIdName(Analytic.Screens.SHOW_SECTIONS, course.getCourseId() + "", course.getTitle());
        Intent intent = getSectionsIntent(sourceActivity, course);
        sourceActivity.startActivity(intent);
        sourceActivity.overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
    }

    @Override
    public void showSections(Activity sourceActivity, @NotNull Course course, boolean joinedRightNow) {
        if (!joinedRightNow) {
            showSections(sourceActivity, course);
        } else {
            analytic.reportEventWithIdName(Analytic.Screens.SHOW_SECTIONS_JOINED, course.getCourseId() + "", course.getTitle());
            Intent intent = getSectionsIntent(sourceActivity, course);
            Bundle bundle = intent.getExtras();
            bundle.putBoolean(SectionsFragment.joinFlag, true);
            intent.putExtras(bundle);
            sourceActivity.startActivity(intent);
        }
    }

    @Override
    public void showUnitsForSection(Activity sourceActivity, @NotNull Section section) {
        analytic.reportEvent(Analytic.Screens.SHOW_UNITS, section.getId() + "");
        Intent intent = getIntentForUnits(sourceActivity, section);
        sourceActivity.startActivity(intent);
        sourceActivity.overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);
    }

    private Intent getIntentForUnits(Activity activity, @NotNull Section section) {
        Intent intent = new Intent(activity, UnitsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(AppConstants.KEY_SECTION_BUNDLE, section);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void showSteps(Activity sourceActivity, Unit unit, Lesson lesson, @Nullable Section section) {
        showSteps(sourceActivity, unit, lesson, false, section);
    }

    @Override
    public void showSteps(Activity sourceActivity, Unit unit, Lesson lesson, boolean backAnimation, @Nullable Section section) {
        analytic.reportEventWithIdName(Analytic.Screens.SHOW_STEP, lesson.getId() + "", lesson.getTitle());
        Intent intent = new Intent(sourceActivity, StepsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstants.KEY_UNIT_BUNDLE, unit);
        bundle.putParcelable(AppConstants.KEY_LESSON_BUNDLE, lesson);
        bundle.putParcelable(AppConstants.KEY_SECTION_BUNDLE, section);
        if (backAnimation) {
            bundle.putBoolean(StepsActivity.Companion.getNeedReverseAnimationKey(), true);
        }
        intent.putExtras(bundle);
        sourceActivity.startActivity(intent);
    }


    @Override
    public void openStepInWeb(Context context, Step step) {
        analytic.reportEvent(Analytic.Screens.OPEN_STEP_IN_WEB, step.getId() + "");
        String url = config.getBaseUrl() + "/lesson/" + step.getLesson() + "/step/" + step.getPosition() + "/?from_mobile_app=true";
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void openSyllabusInWeb(Context context, long courseId) {
        String url = config.getBaseUrl() + "/course/" + courseId + "/syllabus/?from_mobile_app=true";
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void openRemindPassword(AppCompatActivity context) {
        analytic.reportEvent(Analytic.Screens.REMIND_PASSWORD);
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

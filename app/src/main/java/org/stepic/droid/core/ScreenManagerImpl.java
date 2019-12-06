
package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.adaptive.ui.activities.AdaptiveCourseActivity;
import org.stepic.droid.adaptive.ui.activities.AdaptiveOnboardingActivity;
import org.stepic.droid.adaptive.ui.activities.AdaptiveStatsActivity;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.di.AppSingleton;
import org.stepik.android.view.achievement.ui.activity.AchievementsListActivity;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.CollectionDescriptionColors;
import org.stepic.droid.model.CoursesCarouselInfo;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.social.SocialMedia;
import org.stepic.droid.ui.activities.AboutAppActivity;
import org.stepic.droid.ui.activities.AnimatedOnboardingActivity;
import org.stepic.droid.ui.activities.CourseListActivity;
import org.stepic.droid.ui.activities.FeedbackActivity;
import org.stepic.droid.ui.activities.LaunchActivity;
import org.stepic.droid.ui.activities.LoginActivity;
import org.stepic.droid.ui.activities.MainFeedActivity;
import org.stepic.droid.ui.activities.NotificationSettingsActivity;
import org.stepic.droid.ui.activities.PhotoViewActivity;
import org.stepic.droid.ui.activities.RegisterActivity;
import org.stepic.droid.ui.activities.SettingsActivity;
import org.stepic.droid.ui.activities.SplashActivity;
import org.stepic.droid.ui.activities.StoreManagementActivity;
import org.stepic.droid.ui.activities.TagActivity;
import org.stepic.droid.ui.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.IntentExtensionsKt;
import org.stepic.droid.util.UriExtensionsKt;
import org.stepik.android.domain.feedback.model.SupportEmailData;
import org.stepik.android.domain.last_step.model.LastStep;
import org.stepik.android.model.Course;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Tag;
import org.stepik.android.model.Unit;
import org.stepik.android.model.Video;
import org.stepik.android.model.comments.DiscussionThread;
import org.stepik.android.model.user.Profile;
import org.stepik.android.remote.auth.model.TokenType;
import org.stepik.android.view.certificate.ui.activity.CertificatesActivity;
import org.stepik.android.view.comment.ui.activity.CommentsActivity;
import org.stepik.android.view.course.routing.CourseScreenTab;
import org.stepik.android.view.course.ui.activity.CourseActivity;
import org.stepik.android.view.download.ui.activity.DownloadActivity;
import org.stepik.android.view.lesson.ui.activity.LessonActivity;
import org.stepik.android.view.profile.ui.activity.ProfileActivity;
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditActivity;
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditInfoActivity;
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditPasswordActivity;
import org.stepik.android.view.routing.deeplink.BranchDeepLinkRouter;
import org.stepik.android.view.routing.deeplink.BranchRoute;
import org.stepik.android.view.video_player.model.VideoPlayerMediaData;
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

@AppSingleton
public class ScreenManagerImpl implements ScreenManager {
    private final SharedPreferenceHelper sharedPreferences;
    private final Config config;
    private final UserPreferences userPreferences;
    private final Analytic analytic;
    private final Set<BranchDeepLinkRouter> deepLinkRouters;

    @Inject
    public ScreenManagerImpl(Config config, UserPreferences userPreferences, Analytic analytic, SharedPreferenceHelper sharedPreferences, Set<BranchDeepLinkRouter> deepLinkRouters) {
        this.config = config;
        this.userPreferences = userPreferences;
        this.analytic = analytic;
        this.sharedPreferences = sharedPreferences;
        this.deepLinkRouters = deepLinkRouters;
    }

    @Override
    public void showLaunchFromSplash(Activity activity) {
        analytic.reportEvent(Analytic.Screens.SHOW_LAUNCH);
        Intent launchIntent = new Intent(activity, LaunchActivity.class);
        activity.startActivity(launchIntent);
    }

    @Override
    public void showLaunchScreen(Context context) {
        showLaunchScreen(context, false, MainFeedActivity.defaultIndex);
    }

    @Override
    public void showLaunchScreenAfterLogout(Context context) {
        analytic.reportEvent(Analytic.Interaction.SHOW_LAUNCH_SCREEN_AFTER_LOGOUT);
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.putExtra(LaunchActivity.WAS_LOGOUT_KEY, true);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //app context -- new task
        context.startActivity(launchIntent);
    }

    @Override
    public void showLaunchScreen(FragmentActivity activity, @NotNull Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_LAUNCH);
        Intent launchIntent = new Intent(activity, LaunchActivity.class);
        launchIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, course);
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
    public void showAdaptiveStats(Context context, long courseId) {
        Intent intent = new Intent(context, AdaptiveStatsActivity.class);
        intent.putExtra(AppConstants.KEY_COURSE_LONG_ID, courseId);
        context.startActivity(intent);
    }

    @Override
    public void showCoursesList(Activity activity, @NotNull CoursesCarouselInfo info, @Nullable CollectionDescriptionColors descriptionColors) {
        Intent intent = new Intent(activity, CourseListActivity.class);
        intent.putExtra(CourseListActivity.COURSE_LIST_INFO_KEY, info);
        intent.putExtra(CourseListActivity.COURSE_DESCRIPTION_COLORS, (Parcelable) descriptionColors);
        activity.startActivity(intent);
    }

    @Override
    public void showListOfTag(Activity activity, @NotNull Tag tag) {
        TagActivity.Companion.launch(activity, tag);
    }

    @Override
    public void showOnboarding(@NotNull Activity activity) {
        Intent intent = new Intent(activity, AnimatedOnboardingActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void showLaunchScreen(Context context, boolean fromMainFeed, int index) {
        analytic.reportEvent(Analytic.Screens.SHOW_LAUNCH);
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        if (fromMainFeed) {
            launchIntent.putExtra(AppConstants.FROM_MAIN_FEED_FLAG, true);
            launchIntent.putExtra(MainFeedActivity.CURRENT_INDEX_KEY, index);
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //app context -- new task
        context.startActivity(launchIntent);
    }


    @Override
    public void showRegistration(Activity sourceActivity, @Nullable Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_REGISTRATION);
        Intent launchIntent = new Intent(sourceActivity, RegisterActivity.class);
        if (course != null) {
            launchIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, course);
        }
        sourceActivity.startActivity(launchIntent);
    }

    @Override
    public void showLogin(Activity sourceActivity, @Nullable Course course, @Nullable String email) {
        analytic.reportEvent(Analytic.Screens.SHOW_LOGIN);
        Intent loginIntent = new Intent(sourceActivity, LoginActivity.class);
        if (course != null) {
            loginIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, course);
        }
        if (email != null) {
            loginIntent.putExtra(AppConstants.KEY_EMAIL_BUNDLE, email);
        }
        sourceActivity.startActivity(loginIntent);
    }

    @Override
    public void showMainFeedAfterLogin(Activity sourceActivity, @Nullable Course course) {
        analytic.reportEvent(Analytic.Screens.SHOW_MAIN_FEED);
        MainFeedActivity.Companion.launchAfterLogin(sourceActivity, course);
    }

    @Override
    public void showMainFeedFromSplash(Activity sourceActivity) {
        analytic.reportEvent(Analytic.Screens.SHOW_MAIN_FEED);

        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showMainFeed(Context sourceActivity, int indexOfMenu) {
        analytic.reportEvent(Analytic.Screens.SHOW_MAIN_FEED);
        Intent intent = new Intent(sourceActivity, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainFeedActivity.CURRENT_INDEX_KEY, indexOfMenu);
        sourceActivity.startActivity(intent);
    }

    @Override
    public void showCourseDescription(Context context, long courseId) {
        Intent intent = CourseActivity.Companion.createIntent(context, courseId, CourseScreenTab.INFO);
        context.startActivity(intent);
    }

    @Override
    public void showCourseDescription(Context context, @NotNull Course course) {
        showCourseDescription(context, course, false);
    }

    @Override
    public void showCourseDescription(Context context, @NotNull Course course, boolean autoEnroll) {
        showCourseScreen(context, course, autoEnroll, CourseScreenTab.INFO);
    }

    @Override
    public void showCourseModules(Context context, @NotNull Course course) {
        showCourseScreen(context, course, false, CourseScreenTab.SYLLABUS);
    }

    @Override
    public void showCourseScreen(Context context, @NotNull Course course, boolean autoEnroll, CourseScreenTab tab) {
        Intent intent = getIntentForDescription(context, course, autoEnroll, tab);
        context.startActivity(intent);
    }

    private Intent getIntentForDescription(Context context, @NotNull Course course, boolean autoEnroll, CourseScreenTab tab) {
        analytic.reportEvent(Analytic.Screens.SHOW_COURSE_DESCRIPTION);
        Intent intent = CourseActivity.Companion.createIntent(context, course, autoEnroll, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
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
        Context context = App.Companion.getAppContext();
        Intent intent = CertificatesActivity.Companion.createIntent(context, userPreferences.getUserId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public void showCertificates(Context context) {
        showCertificates(context, userPreferences.getUserId());
    }

    @Override
    public void showCertificates(Context context, long userId) {
        Intent intent = CertificatesActivity.Companion.createIntent(context, userId);
        context.startActivity(intent);
    }

    @Override
    public void showDownloads(Context context) {
        Intent intent = DownloadActivity.Companion.createIntent(context);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    public void showCatalog(Context context) {
        context.startActivity(getCatalogIntent(context));
    }

    @Override
    public Intent getCatalogIntent(Context context) {
        int index = MainFeedActivity.CATALOG_INDEX;
        return getFromMainActivityIntent(context, index);
    }

    private Intent getFromMainActivityIntent(Context context, int index) {
        Intent intent = new Intent(context, MainFeedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt(MainFeedActivity.CURRENT_INDEX_KEY, index);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void showVideo(@NotNull Fragment sourceFragment, @NotNull VideoPlayerMediaData videoPlayerMediaData, boolean isAutoplayEnabled) {
        analytic.reportEvent(Analytic.Screens.TRY_OPEN_VIDEO);
        boolean isOpenExternal = userPreferences.isOpenInExternal();
        if (isOpenExternal) {
            analytic.reportEvent(Analytic.Video.OPEN_EXTERNAL);
        } else {
            analytic.reportEvent(Analytic.Video.OPEN_NATIVE);
        }

        final Context context = sourceFragment.requireContext();

        if (!isOpenExternal) {
            sourceFragment.startActivityForResult(
                    VideoPlayerActivity.Companion.createIntent(context, videoPlayerMediaData, isAutoplayEnabled),
                    VideoPlayerActivity.REQUEST_CODE
            );
        } else {
            @Nullable
            final Video cachedVideo = videoPlayerMediaData.getCachedVideo();

            @Nullable
            final Video externalVideo = videoPlayerMediaData.getExternalVideo();

            String videoPath = null;
            if (cachedVideo != null && cachedVideo.getUrls() != null && !cachedVideo.getUrls().isEmpty()) {
                videoPath = cachedVideo.getUrls().get(0).getUrl();
            } else if (externalVideo != null && externalVideo.getUrls() != null && !externalVideo.getUrls().isEmpty()) {
                videoPath = externalVideo.getUrls().get(0).getUrl();
            }
            Uri videoUri = Uri.parse(videoPath);
            String scheme = videoUri.getScheme();
            if (scheme == null && videoPath != null) {
                final File file = new File(videoPath);
                videoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + AppConstants.FILE_PROVIDER_AUTHORITY, file);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
            intent.setDataAndType(videoUri, "video/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            try {
                sourceFragment.startActivity(intent);
            } catch (Exception ex) {
                analytic.reportError(Analytic.Error.NOT_PLAYER, ex);
                Toast.makeText(context, R.string.not_video_player_error, Toast.LENGTH_LONG).show();
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
    public void showNotificationSettings(Activity sourceActivity) {
        analytic.reportEvent(Analytic.Screens.SHOW_NOTIFICATION_SETTINGS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //show system settings
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, sourceActivity.getPackageName());
            sourceActivity.startActivity(intent);
        } else {
            //show app notification settings
            //(SDK < 26)
            Intent intent = new Intent(sourceActivity, NotificationSettingsActivity.class);
            sourceActivity.startActivity(intent);
            sourceActivity.overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
        }
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
    public void openProfile(@NonNull Context context, long userId) {
        context.startActivity(ProfileActivity.Companion.createIntent(context, userId));
    }

    @Override
    public void openFeedbackActivity(Activity activity) {
        final Intent intent = new Intent(activity, FeedbackActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public Intent getMyCoursesIntent(@NotNull Context context) {
        int index = MainFeedActivity.HOME_INDEX;
        return getFromMainActivityIntent(context, index);
    }

    @Nullable
    @Override
    public Intent getProfileIntent(@NotNull Context context) {
//        Intent intent = new Intent(context, ProfileActivity.class);
        Intent intent = new Intent(context, org.stepik.android.view.profile.ui.activity.ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public Intent getMyProfileIntent(@NotNull Context context) {
        return getFromMainActivityIntent(context, MainFeedActivity.PROFILE_INDEX);
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
    public void continueAdaptiveCourse(Activity activity, Course course) {
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(activity)
                .addNextIntent(new Intent(activity, MainFeedActivity.class)
                .setAction(AppConstants.INTERNAL_STEPIK_ACTION));

        Intent adaptiveCourseIntent = new Intent(activity, AdaptiveCourseActivity.class);
        adaptiveCourseIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, course);
        taskStackBuilder.addNextIntent(adaptiveCourseIntent);

        if (sharedPreferences.isFirstAdaptiveCourse()) {
            taskStackBuilder.addNextIntent(new Intent(activity, AdaptiveOnboardingActivity.class));
        }

        taskStackBuilder.startActivities();
    }

    @Override
    public void continueCourse(Activity activity, long courseId, @NotNull LastStep lastStep) {
        Intent stepsIntent = LessonActivity.Companion.createIntent(activity, lastStep);

        Intent courseIntent = CourseActivity.Companion.createIntent(activity,
                courseId, CourseScreenTab.SYLLABUS);

        TaskStackBuilder.create(activity)
                .addNextIntent(new Intent(activity, MainFeedActivity.class)
                        .setAction(AppConstants.INTERNAL_STEPIK_ACTION))
                .addNextIntent(courseIntent)
                .addNextIntent(stepsIntent)
                .startActivities();
    }

    @Override
    public void continueCourse(Activity activity, @NotNull LastStep lastStep) {
        Intent intent = LessonActivity.Companion.createIntent(activity, lastStep);
        activity.startActivity(intent);
    }

    @Override
    public void openInWeb(Activity context, String path) {
        analytic.reportEventWithName(Analytic.Screens.OPEN_LINK_IN_WEB, path);
        final Intent intent = getOpenInWebIntent(path);
        context.startActivity(intent);
    }

    @Override
    public void addCertificateToLinkedIn(CertificateViewItem certificateViewItem) {
        // TODO: 19/10/2017 linkedin exporting is not working due to changing API params is not filled

        StringBuilder sb = new StringBuilder();
        sb.append(AppConstants.LINKEDIN_ADD_URL);
        sb.append("_ed=");//linkedin id parameter
        sb.append(AppConstants.LINKEDIN_ED_ID);
        sb.append("&pfCertificationName="); // linkedin cert name
        sb.append(URLEncoder.encode(certificateViewItem.getTitle()));
        sb.append("&pfCertificationUrl=");//linkedin certificate url
        sb.append(certificateViewItem.getCertificate().getUrl());


        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(sb.toString()));
        App.Companion.getAppContext().startActivity(intent);
    }

    @Override
    public void showPdfInBrowserByGoogleDocs(Activity activity, String fullPath) {
        String googleDocsUrl = "https://docs.google.com/viewer?url=";
        openInWeb(activity, googleDocsUrl + fullPath);
    }

    @Override
    public void openComments(Activity context, @NonNull DiscussionThread discussionThread, @NonNull Step step, @Nullable Long discussionId, boolean needOpenForm) {
        analytic.reportEvent(Analytic.Screens.OPEN_COMMENT);
        context.startActivity(CommentsActivity.Companion.createIntent(context, step, discussionThread, discussionId, needOpenForm));
    }

    @Override
    public void showSteps(Activity sourceActivity, @NotNull Unit unit, @NotNull Lesson lesson, @NotNull Section section) {
        showSteps(sourceActivity, unit, lesson, section, false, false);
    }

    @Override
    public void showSteps(Activity sourceActivity, @NotNull Unit unit, @NotNull Lesson lesson, @NotNull Section section, boolean backAnimation, boolean isAutoplayEnabled) {
        analytic.reportEventWithIdName(Analytic.Screens.SHOW_STEP, lesson.getId() + "", lesson.getTitle());
        Intent intent = LessonActivity.Companion.createIntent(sourceActivity, section, unit, lesson, backAnimation, isAutoplayEnabled);
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
    public void openDiscussionInWeb(Context context, @NonNull Step step, @NonNull DiscussionThread discussionThread, long discussionId) {
        String url =
                config.getBaseUrl() +
                        "/lesson/" + step.getLesson() +
                        "/step/" + step.getPosition() +
                        "/?from_mobile_app=true" +
                        "&discussion=" + discussionId +
                        "&thread=" + discussionThread.getThread()
                ;
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void openSubmissionInWeb(Context context, long stepId, long submissionId) {
        String url =
                config.getBaseUrl() +
                        "/submissions/" + stepId +
                        "/" + submissionId +
                        "/?from_mobile_app=true"
                ;
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void openSyllabusInWeb(Context context, long courseId) {
        openCourseTabInWeb(context, courseId, CourseScreenTab.SYLLABUS.getPath(), null);
    }

    @Override
    public void openCoursePurchaseInWeb(Context context, long courseId, @Nullable Map<String, List<String>> queryParams) {
        openCourseTabInWeb(context, courseId, CourseScreenTab.PAY.getPath(), queryParams);
    }

    private void openCourseTabInWeb(Context context, long courseId, String tab, @Nullable Map<String, List<String>> queryParams) {
        final String url = config.getBaseUrl() + "/course/" + courseId + "/" + tab + "/";
        final Uri.Builder uriBuilder = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("from_mobile_app", "true");

        if (queryParams != null) {
            UriExtensionsKt.Uri_Builder_appendAllQueryParameters(uriBuilder, queryParams);
        }

        final Uri uri = uriBuilder.build();

        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.getBaseUrl()));

        final List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(browserIntent, 0);
        final ArrayList<Intent> activityIntents = new ArrayList<>();

        for (final ResolveInfo resolveInfo : resolveInfoList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.startsWith("org.stepic.droid") && !packageName.startsWith("org.stepik.android")) {
                final Intent newIntent = new Intent(Intent.ACTION_VIEW, uri);
                newIntent.setPackage(packageName);
                activityIntents.add(newIntent);
            }
        }

        if (!activityIntents.isEmpty()) {
            final Intent chooserIntent = Intent.createChooser(activityIntents.remove(0), context.getString(R.string.course_purchase_link_chooser_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, activityIntents.toArray(new Parcelable[] {}));

            context.startActivity(chooserIntent);
        }
    }

    @Override
    public void openRemindPassword(AppCompatActivity context) {
        analytic.reportEvent(Analytic.Screens.REMIND_PASSWORD);
        DialogFragment dialogFragment = RemindPasswordDialogFragment.newInstance();
        dialogFragment.show(context.getSupportFragmentManager(), null);
    }

    @Override
    public void showAchievementsList(Context context, long userId, boolean isMyProfile) {
        Intent intent = AchievementsListActivity.Companion.createIntent(context, userId, isMyProfile);
        context.startActivity(intent);
    }

    @Override
    public void openDeepLink(Context context, BranchRoute route) {
        for (BranchDeepLinkRouter router : deepLinkRouters) {
            if (router.handleBranchRoute(this, context, route)) {
                return;
            }
        }
    }

    @Override
    public void showProfileEdit(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
        }
        context.startActivity(ProfileEditActivity.Companion.createIntent(context));
    }

    @Override
    public void showProfileEditInfo(Activity activity, Profile profile) {
        activity.overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
        activity.startActivityForResult(ProfileEditInfoActivity.Companion.createIntent(activity, profile), ProfileEditInfoActivity.REQUEST_CODE);
    }

    @Override
    public void showProfileEditPassword(Activity activity, long profileId) {
        activity.overridePendingTransition(org.stepic.droid.R.anim.push_up, org.stepic.droid.R.anim.no_transition);
        activity.startActivityForResult(ProfileEditPasswordActivity.Companion.createIntent(activity, profileId), ProfileEditPasswordActivity.REQUEST_CODE);
    }

    @Override
    public void openTextFeedBack(Context context, SupportEmailData supportEmailData) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {supportEmailData.getMailTo()});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, supportEmailData.getSubject());
        emailIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "org.stepic.droid.provider", supportEmailData.getBody()));
        context.startActivity(IntentExtensionsKt.createEmailOnlyChooserIntent(emailIntent, context, context.getString(R.string.feedback_email_chooser_title)));
    }

    @Override
    public void openSocialMediaLink(Context context, String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(browserIntent);
    }

    @Override
    public void openSocialMediaLink(Context context, SocialMedia socialLink) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(socialLink.getLink()));
        context.startActivity(browserIntent);
    }

    @Override
    public void loginWithSocial(FragmentActivity activity, ISocialType type) {
        String socialIdentifier = type.getIdentifier();
        String url = config.getBaseUrl() + "/accounts/" + socialIdentifier + "/login?next=/oauth2/authorize/?" + Uri.encode("client_id=" + config.getOAuthClientId(TokenType.SOCIAL) + "&response_type=code");
        Uri uri = Uri.parse(url);
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        activity.startActivity(intent);
    }
}

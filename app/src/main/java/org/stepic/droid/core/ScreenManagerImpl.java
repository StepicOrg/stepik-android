
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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
import org.stepic.droid.features.achievements.ui.activity.AchievementsListActivity;
import org.stepic.droid.util.UriExtensionsKt;
import org.stepik.android.view.course.routing.CourseScreenTab;
import org.stepik.android.view.course.ui.activity.CourseActivity;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.CollectionDescriptionColors;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CoursesCarouselInfo;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;
import org.stepik.android.model.Video;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.services.ViewPusher;
import org.stepic.droid.ui.activities.AboutAppActivity;
import org.stepic.droid.ui.activities.AnimatedOnboardingActivity;
import org.stepic.droid.ui.activities.CertificatesActivity;
import org.stepic.droid.ui.activities.CommentsActivity;
import org.stepic.droid.ui.activities.CourseListActivity;
import org.stepic.droid.ui.activities.DownloadsActivity;
import org.stepic.droid.ui.activities.FeedbackActivity;
import org.stepic.droid.ui.activities.LaunchActivity;
import org.stepic.droid.ui.activities.LoginActivity;
import org.stepic.droid.ui.activities.MainFeedActivity;
import org.stepic.droid.ui.activities.NewCommentActivity;
import org.stepic.droid.ui.activities.NotificationSettingsActivity;
import org.stepic.droid.ui.activities.PhotoViewActivity;
import org.stepic.droid.ui.activities.ProfileActivity;
import org.stepic.droid.ui.activities.RegisterActivity;
import org.stepic.droid.ui.activities.SettingsActivity;
import org.stepic.droid.ui.activities.SplashActivity;
import org.stepic.droid.ui.activities.StepsActivity;
import org.stepic.droid.ui.activities.StoreManagementActivity;
import org.stepic.droid.ui.activities.TagActivity;
import org.stepic.droid.ui.activities.TextFeedbackActivity;
import org.stepic.droid.ui.dialogs.RemindPasswordDialogFragment;
import org.stepic.droid.ui.fragments.CommentsFragment;
import org.stepic.droid.util.AndroidVersionKt;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.web.ViewAssignment;
import org.stepik.android.model.Tag;
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
        intent.putExtra(PhotoViewActivity.Companion.getPathKey(), path);
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
    public void showTextFeedback(Activity sourceActivity) {
        analytic.reportEvent(Analytic.Screens.SHOW_TEXT_FEEDBACK);
        Intent launchIntent = new Intent(sourceActivity, TextFeedbackActivity.class);
        sourceActivity.startActivityForResult(launchIntent, TextFeedbackActivity.Companion.getRequestCode());
        sourceActivity.overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down);
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
        Intent intent = new Intent(context, CertificatesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public void showCertificates(Context context) {
        showMainFeed(context, MainFeedActivity.CERTIFICATE_INDEX);
    }

    @Override
    public void showDownloads(Context context) {
        Intent intent = new Intent(context, DownloadsActivity.class);
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
    public void showVideo(Activity sourceActivity, @NotNull VideoPlayerMediaData videoPlayerMediaData) {
        analytic.reportEvent(Analytic.Screens.TRY_OPEN_VIDEO);
        boolean isOpenExternal = userPreferences.isOpenInExternal();
        if (isOpenExternal) {
            analytic.reportEvent(Analytic.Video.OPEN_EXTERNAL);
        } else {
            analytic.reportEvent(Analytic.Video.OPEN_NATIVE);
        }

        boolean isCompatible = AndroidVersionKt.isJellyBeanOrLater();
        if (!isCompatible) {
            analytic.reportEvent(Analytic.Video.NOT_COMPATIBLE);
        }

        if (isCompatible && !isOpenExternal) {
            sourceActivity.startActivity(VideoPlayerActivity.Companion.createIntent(sourceActivity, videoPlayerMediaData));
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
                videoUri = FileProvider.getUriForFile(sourceActivity, sourceActivity.getApplicationContext().getPackageName() + AppConstants.FILE_PROVIDER_AUTHORITY, file);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
            intent.setDataAndType(videoUri, "video/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
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
    public void openProfile(Activity activity) {
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
        int index = MainFeedActivity.HOME_INDEX;
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
    public void continueCourse(Activity activity, long courseId, long unitId, long lessonId, long stepId) {
        String testStepPath = StringUtil.getUriForStepByIds(config.getBaseUrl(), lessonId, unitId, stepId);

        Intent stepsIntent = new Intent(activity, StepsActivity.class)
                .setAction(AppConstants.INTERNAL_STEPIK_ACTION)
                .putExtra(StepsActivity.EXTRA_IS_STEP_ID_WAS_PASSED, true)
                .setData(Uri.parse(testStepPath));

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
        sb.append(certificateViewItem.getFullPath());


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
    public void openComments(Activity context, @Nullable String discussionProxyId, long stepId) {
        openComments(context, discussionProxyId, stepId, false);
    }

    @Override
    public void openComments(Activity context, String discussionProxyId, long stepId, boolean needOpenForm) {
        if (discussionProxyId == null) {
            analytic.reportEvent(Analytic.Screens.OPEN_COMMENT_NOT_AVAILABLE);
            Toast.makeText(context, R.string.comment_denied, Toast.LENGTH_SHORT).show();
        } else {
            analytic.reportEvent(Analytic.Screens.OPEN_COMMENT);
            Intent intent = new Intent(context, CommentsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(CommentsActivity.Companion.getKeyDiscussionProxyId(), discussionProxyId);
            bundle.putLong(CommentsActivity.Companion.getKeyStepId(), stepId);
            bundle.putBoolean(CommentsActivity.Companion.getKeyNeedInstaOpenForm(), needOpenForm);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }


    @Override
    public void openNewCommentForm(CommentsFragment commentsFragment, Long target, @Nullable Long parent) {
        if (sharedPreferences.getAuthResponseFromStore() != null) {
            analytic.reportEvent(Analytic.Screens.OPEN_WRITE_COMMENT);
            Intent intent = new Intent(commentsFragment.getActivity(), NewCommentActivity.class);
            Bundle bundle = new Bundle();
            if (parent != null) {
                bundle.putLong(NewCommentActivity.Companion.getKeyParent(), parent);
            }
            bundle.putLong(NewCommentActivity.Companion.getKeyTarget(), target);
            intent.putExtras(bundle);
            commentsFragment.startActivityForResult(intent, NewCommentActivity.Companion.getRequestCode());
        } else {
            Toast.makeText(commentsFragment.getContext(), R.string.anonymous_write_comment, Toast.LENGTH_SHORT).show();
        }
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
            bundle.putBoolean(StepsActivity.needReverseAnimationKey, true);
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
    public void openCoursePurchaseInWeb(Context context, long courseId, @Nullable Map<String, List<String>> queryParams) {
        String url = config.getBaseUrl() + "/course/" + courseId + "/pay/";
        final Uri.Builder uriBuilder = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("from_mobile_app", "true");

        if (queryParams != null) {
            UriExtensionsKt.Uri_Builder_appendAllQueryParameters(uriBuilder, queryParams);
        }

        final Uri uri = uriBuilder.build();

        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        final List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
        final ArrayList<Intent> activityIntents = new ArrayList<>();

        for (final ResolveInfo resolveInfo : resolveInfoList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.startsWith("org.stepic.droid") && !packageName.startsWith("org.stepik.android")) {
                final Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                intent1.setPackage(packageName);
                activityIntents.add(intent1);
            }
        }

        if (!activityIntents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(activityIntents.remove(0), "Open link with");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, activityIntents.toArray(new Parcelable[] {}));

            context.startActivity(chooserIntent);
        }
    }

    @Override
    public void openRemindPassword(AppCompatActivity context) {
        analytic.reportEvent(Analytic.Screens.REMIND_PASSWORD);
        android.support.v4.app.DialogFragment dialogFragment = RemindPasswordDialogFragment.newInstance();
        dialogFragment.show(context.getSupportFragmentManager(), null);
    }

    @Override
    public void pushToViewedQueue(ViewAssignment viewAssignmentWrapper) {

        Intent loadIntent = new Intent(App.Companion.getAppContext(), ViewPusher.class);

        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, viewAssignmentWrapper.getStep());
        loadIntent.putExtra(AppConstants.KEY_ASSIGNMENT_BUNDLE, viewAssignmentWrapper.getAssignment());
        App.Companion.getAppContext().startService(loadIntent);
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
}

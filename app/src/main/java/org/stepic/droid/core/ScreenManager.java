package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.CollectionDescriptionColors;
import org.stepic.droid.social.SocialMedia;
import org.stepic.droid.model.CoursesCarouselInfo;
import org.stepik.android.domain.feedback.model.SupportEmailData;
import org.stepik.android.domain.last_step.model.LastStep;
import org.stepik.android.model.Course;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepik.android.model.Step;
import org.stepik.android.model.Tag;
import org.stepik.android.model.Unit;
import org.stepik.android.model.user.Profile;
import org.stepik.android.view.course.routing.CourseScreenTab;
import org.stepik.android.view.routing.deeplink.BranchRoute;
import org.stepik.android.view.video_player.model.VideoPlayerMediaData;

import java.util.List;
import java.util.Map;

public interface ScreenManager {

    void showLaunchFromSplash(Activity activity);

    void showLaunchScreen(Context context);

    void showLaunchScreenAfterLogout(Context context);

    void showLaunchScreen(Context context, boolean fromMainFeed, int indexInMenu);

    void showRegistration(Activity sourceActivity, @Nullable Course course);

    void showLogin(Activity sourceActivity, @Nullable Course course, @Nullable String email);

    void showMainFeedAfterLogin(Activity sourceActivity, @Nullable Course course);

    void showMainFeedFromSplash(Activity sourceActivity);

    void showMainFeed(Context sourceActivity, int indexOfMenu);

    void showPdfInBrowserByGoogleDocs(Activity activity, String fullPath);

    void openComments(Activity context, String discussionProxyId, long stepId, @Nullable Long discussionId, boolean needOpenForm);

    void showSteps(Activity sourceActivity, @NotNull Unit unit, @NotNull Lesson lesson, @NotNull Section section);

    void showSteps(Activity sourceActivity, @NotNull Unit unit, @NotNull Lesson lesson, boolean backAnimation, @NotNull Section section);

    void openStepInWeb(Context context, Step step);

    void openRemindPassword(AppCompatActivity context);

    void showCourseDescription(Context context, long courseId);
    void showCourseDescription(Context context, @NotNull Course course);
    void showCourseDescription(Context context, @NotNull Course course, boolean autoEnroll);
    void showCourseModules(Context context, @NotNull Course course);
    void showCourseScreen(Context context, @NotNull Course course, boolean autoEnroll, CourseScreenTab tab);

    void showStoreWithApp(Activity sourceActivity);

    void showDownloads(Context context);

    void showCatalog(Context context);

    Intent getCatalogIntent(Context context);

    void showVideo(Activity sourceActivity, @NotNull VideoPlayerMediaData videoPlayerMediaData);

    void showSettings(Activity sourceActivity);

    void showNotificationSettings(Activity sourceActivity);

    void showStorageManagement(Activity activity);

    void openInWeb(Activity context, String path);

    void addCertificateToLinkedIn(CertificateViewItem certificateViewItem);

    void showCertificates(Context context);

    void showCertificates(Context context, long userId);

    void openSyllabusInWeb(Context context, long courseId);

    void openCoursePurchaseInWeb(Context context, long courseId, @Nullable Map<String, List<String>> queryParams);

    Intent getCertificateIntent();

    Intent getOpenInWebIntent(String path);

    void openProfile(Activity activity);

    void openProfile(Activity activity, long userId);

    void openFeedbackActivity(Activity activity);

    Intent getMyCoursesIntent(@NotNull Context context);

    Intent getProfileIntent(@NotNull Context context);

    Intent getMyProfileIntent(@NotNull Context context);

    void openSplash(Context context);

    void openAboutActivity(Activity activity);

    void openPrivacyPolicyWeb(Activity activity);

    void openTermsOfServiceWeb(Activity activity);

    void continueAdaptiveCourse(Activity activity, Course course);

    void continueCourse(Activity activity, long courseId, @NotNull LastStep lastStep);
    void continueCourse(Activity activity, @NotNull LastStep lastStep);

    void showLaunchScreen(FragmentActivity activity, @NotNull Course course);

    void openImage(Context context, String path);

    void showAdaptiveStats(Context context, long courseId);


    void showCoursesList(Activity activity, @NotNull CoursesCarouselInfo info, @Nullable CollectionDescriptionColors collectionDescriptionColors);

    void showListOfTag(Activity activity, @NotNull Tag tag);

    void showOnboarding(@NotNull Activity activity);

    void showAchievementsList(Context context, long userId, boolean isMyProfile);

    void openDeepLink(Context context, BranchRoute route);

    void showProfileEdit(Context context);
    void showProfileEditInfo(Activity activity, Profile profile);
    void showProfileEditPassword(Activity activity, long profileId);

    void openTextFeedBack(Context context, SupportEmailData supportEmailData);

    void openSocialMediaLink(Context context, SocialMedia socialLink);
}

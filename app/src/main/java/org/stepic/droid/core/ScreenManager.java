package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.CollectionDescriptionColors;
import org.stepik.android.model.structure.Course;
import org.stepic.droid.model.CoursesCarouselInfo;
import org.stepik.android.model.structure.Lesson;
import org.stepik.android.model.structure.Section;
import org.stepic.droid.model.Step;
import org.stepik.android.model.structure.Unit;
import org.stepik.android.model.structure.Video;
import org.stepic.droid.ui.fragments.CommentsFragment;
import org.stepic.droid.web.ViewAssignment;
import org.stepik.android.model.structure.Tag;

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

    void showCourseDescription(Fragment sourceActivity, @NotNull Course course);

    void showPdfInBrowserByGoogleDocs(Activity activity, String fullPath);

    void openComments(Activity context, String discussionProxyId, long stepId);

    void openComments(Activity context, String discussionProxyId, long stepId, boolean needOpenForm);

    void openNewCommentForm(CommentsFragment commentsFragment, Long target, @Nullable Long parent);

    void showSections(Activity sourceActivity, @NotNull Course course);

    void showSections(Activity sourceActivity, @NotNull Course course, boolean joinedRightNow);

    void showUnitsForSection(Activity sourceActivity, @NotNull Section section);

    void showSteps(Activity sourceActivity, Unit unit, Lesson lesson, @Nullable Section section);

    void showSteps(Activity sourceActivity, Unit unit, Lesson lesson, boolean backAnimation, @Nullable Section section);

    void openStepInWeb(Context context, Step step);

    void openRemindPassword(AppCompatActivity context);

    void pushToViewedQueue(ViewAssignment viewAssignmentWrapper);

    void showCourseDescription(Context context, @NotNull Course course);

    void showCourseDescription(Activity sourceActivity, @NotNull Course course, boolean instaEnroll);

    void showTextFeedback(Activity sourceActivity);

    void showStoreWithApp(Activity sourceActivity);

    void showDownloads(Context context);

    void showCatalog(Context context);

    Intent getCatalogIntent(Context context);

    void showVideo(Activity sourceActivity, @Nullable Video cachedVideo, @Nullable Video externalVideo);

    void showSettings(Activity sourceActivity);

    void showNotificationSettings(Activity sourceActivity);

    void showStorageManagement(Activity activity);

    void openInWeb(Activity context, String path);

    void addCertificateToLinkedIn(CertificateViewItem certificateViewItem);

    void showCertificates(Context context);

    void openSyllabusInWeb(Context context, long courseId);

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

    void continueCourse(Activity activity, long courseId, Section section, long lessonId, long unitId, long stepPosition);

    void continueCourse(Activity activity, long courseId, Section section, long lessonId, long unitId, long stepPosition, boolean joinedRightNow);

    void showLaunchScreen(FragmentActivity activity, @NotNull Course course);

    void openImage(Context context, String path);

    void showAdaptiveStats(Context context, long courseId);


    void showCoursesList(Activity activity, @NotNull CoursesCarouselInfo info, @Nullable CollectionDescriptionColors collectionDescriptionColors);

    void showListOfTag(Activity activity, @NotNull Tag tag);

    void showOnboarding(@NotNull Activity activity);

    void showAchievementsList(Context context, long userId, boolean isMyProfile);
}

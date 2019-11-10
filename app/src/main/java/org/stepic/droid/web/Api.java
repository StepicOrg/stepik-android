package org.stepic.droid.web;

import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;
import org.stepic.droid.web.model.adaptive.RecommendationsResponse;
import org.stepik.android.model.Tag;
import org.stepik.android.model.adaptive.RatingItem;
import org.stepik.android.model.adaptive.RecommendationReaction;
import org.stepik.android.remote.assignment.model.AssignmentResponse;
import org.stepik.android.remote.attempt.model.AttemptResponse;
import org.stepik.android.remote.auth.model.StepikProfileResponse;
import org.stepik.android.remote.certificate.model.CertificateResponse;
import org.stepik.android.remote.course.model.CourseResponse;
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse;
import org.stepik.android.remote.course.model.EnrollmentRequest;
import org.stepik.android.remote.email_address.model.EmailAddressResponse;
import org.stepik.android.remote.last_step.model.LastStepResponse;
import org.stepik.android.remote.progress.model.ProgressResponse;
import org.stepik.android.remote.step.model.StepResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;
import org.stepik.android.remote.user_activity.model.UserActivityResponse;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Call;


public interface Api {

    enum TokenType {
        social, loginPassword
    }

    Single<UserCoursesResponse> getUserCourses(int page);

    Single<CourseResponse> getPopularCourses(int page);

    Call<StepikProfileResponse> getUserProfile();

    Call<UserResponse> getUsers(long[] userIds);

    Single<UserResponse> getUsersRx(long[] userIds);

    Completable joinCourse(EnrollmentRequest enrollmentRequest);

    Completable dropCourse(long courseId);

    /**
     * Max number of  units defined in AppConstants
     */
    Call<UnitResponse> getUnits(List<Long> units);

    Single<UnitResponse> getUnits(long courseId, long lessonId);

    Single<StepResponse> getSteps(long[] steps);

    Single<StepResponse> getStepsByLessonId(long lessonId);

    Call<ProgressResponse> getProgresses(String[] progresses);

    Single<ProgressResponse> getProgressesReactive(String[] progresses);

    Single<AssignmentResponse> getAssignments(long[] assignmentsIds);

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery);

    Single<QueriesResponse> getSearchQueries(String query);

    Call<CourseResponse> getCourses(int page, long[] ids);

    Single<CourseResponse> getCoursesReactive(int page, @NotNull long[] ids);

    Single<CourseResponse> getCoursesReactive(@NotNull long[] ids);

    Single<AttemptResponse> createNewAttemptReactive(long stepId);

    Single<AttemptResponse> getExistingAttemptsReactive(long stepId);

    Single<AttemptResponse> getExistingAttemptsReactive(long[] attemptIds);

    Call<Void> remindPassword(String email);

    Call<EmailAddressResponse> getEmailAddresses(long[] ids);

    Call<DeviceResponse> getDevicesByRegistrationId(String token);

    Call<DeviceResponse> renewDeviceRegistration(long deviceId, String token);

    Call<DeviceResponse> registerDevice(String token);

    Call<CourseResponse> getCourse(long id);

    Call<Void> setReadStatusForNotification(long notificationId, boolean isRead);

    Completable setReadStatusForNotificationReactive(long notificationId, boolean isRead);

    Single<CertificateResponse> getCertificates(long userId, int page);

    Call<NotificationResponse> getNotifications(NotificationCategory notificationCategory, int page);

    Call<Void> markAsReadAllType(@NotNull NotificationCategory notificationCategory);

    Single<NotificationStatusesResponse> getNotificationStatuses();

    Call<UserActivityResponse> getUserActivities(long userId);

    Single<UserActivityResponse> getUserActivitiesReactive(long userId);

    Single<LastStepResponse> getLastStepResponse(@NotNull String lastStepId);

    Single<CourseCollectionsResponse> getCourseCollections(String language);

    Single<CourseReviewSummaryResponse> getCourseReviewSummaries(long[] reviewSummaryIds);

    Single<TagResponse> getFeaturedTags();

    Single<SearchResultResponse> getSearchResultsOfTag(int page, @NotNull Tag tag);

    Single<RecommendationsResponse> getNextRecommendations(long courseId, int count);

    Completable createReaction(RecommendationReaction reaction);

    Single<List<RatingItem>> getRating(long courseId, int count, int days);

    Completable putRating(long courseId, long exp);

    Single<RatingRestoreResponse> restoreRating(long courseId);
}

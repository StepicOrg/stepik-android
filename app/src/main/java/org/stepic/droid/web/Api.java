package org.stepic.droid.web;

import android.support.v4.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse;
import org.stepik.android.model.adaptive.RatingItem;
import org.stepik.android.model.Course;
import org.stepic.droid.model.NotificationCategory;
import org.stepik.android.model.Submission;
import org.stepik.android.model.adaptive.RecommendationReaction;
import org.stepik.android.model.comments.Vote;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.web.model.adaptive.RatingRestoreResponse;
import org.stepic.droid.web.model.adaptive.RecommendationsResponse;
import org.stepik.android.model.Tag;
import org.stepik.android.model.Reply;
import org.stepik.android.remote.assignment.model.AssignmentResponse;
import org.stepik.android.remote.attempt.model.AttemptResponse;
import org.stepik.android.remote.comment.model.CommentResponse;
import org.stepik.android.remote.course.model.CourseResponse;
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse;
import org.stepik.android.remote.course.model.EnrollmentRequest;
import org.stepik.android.remote.email_address.model.EmailAddressResponse;
import org.stepik.android.remote.last_step.model.LastStepResponse;
import org.stepik.android.remote.lesson.model.LessonResponse;
import org.stepik.android.remote.progress.model.ProgressResponse;
import org.stepik.android.remote.section.model.SectionResponse;
import org.stepik.android.remote.step.model.StepResponse;
import org.stepik.android.remote.submission.model.SubmissionResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;
import org.stepik.android.remote.user_activity.model.UserActivityResponse;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;


public interface Api {

    enum TokenType {
        social, loginPassword
    }

    Call<AuthenticationStepikResponse> authWithNativeCode(String code, SocialManager.SocialType type, @Nullable String email);

    Call<AuthenticationStepikResponse> authWithLoginPassword(String login, String password);

    Call<AuthenticationStepikResponse> authWithCode(String code);

    Call<RegistrationResponse> signUp(String firstName, String secondName, String email, String password);

    Single<UserCoursesResponse> getUserCourses(int page);

    Single<CourseResponse> getPopularCourses(int page);

    Call<StepicProfileResponse> getUserProfile();

    Call<UserResponse> getUsers(long[] userIds);

    Single<UserResponse> getUsersRx(long[] userIds);

    Completable joinCourse(EnrollmentRequest enrollmentRequest);

    Completable dropCourse(long courseId);

    @Nullable
    Call<Void> dropCourse(@NotNull Course course);

    Call<SectionResponse> getSections(long[] sectionsIds);

    Single<SectionResponse> getSectionsRx(long[] sectionsIds);

    /**
     * Max number of  units defined in AppConstants
     */
    Call<UnitResponse> getUnits(List<Long> units);

    Single<UnitResponse> getUnitsRx(long[] units);

    Single<UnitResponse> getUnits(long courseId, long lessonId);

    Call<LessonResponse> getLessons(long[] lessons);

    Single<LessonResponse> getLessonsRx(long[] lessons);

    Single<LessonResponse> getLessons(long lessonId);

    Single<StepResponse> getSteps(long[] steps);

    Single<StepResponse> getStepsReactive(long[] steps);

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

    Call<AttemptResponse> createNewAttempt(long stepId);

    Single<AttemptResponse> createNewAttemptReactive(long stepId);

    Call<SubmissionResponse> createNewSubmission(Reply reply, long attemptId);

    Single<SubmissionResponse> createNewSubmissionReactive(Submission submission);

    Call<AttemptResponse> getExistingAttempts(long stepId);

    Single<AttemptResponse> getExistingAttemptsReactive(long stepId);

    Call<SubmissionResponse> getSubmissions(long attemptId);

    Single<SubmissionResponse> getSubmissionsReactive(long attemptId);

    Call<SubmissionResponse> getSubmissionForStep(long stepId);

    Single<SubmissionResponse> getSubmissionForStepReactive(long stepId);

    Call<Void> remindPassword(String email);

    Call<EmailAddressResponse> getEmailAddresses(long[] ids);

    Call<DeviceResponse> getDevices();

    Call<DeviceResponse> getDevicesByRegistrationId(String token);

    Call<DeviceResponse> renewDeviceRegistration(long deviceId, String token);

    Call<DeviceResponse> registerDevice(String token);

    Call<CourseResponse> getCourse(long id);

    Call<Void> setReadStatusForNotification(long notificationId, boolean isRead);

    Completable setReadStatusForNotificationReactive(long notificationId, boolean isRead);

    Call<Void> removeDevice(long deviceId);

    Call<CommentResponse> getCommentAnd20Replies(long commentId);

    Call<CommentResponse> getCommentsByIds(long[] commentIds);

    Call<CommentResponse> postComment(String text, long target /*for example, related step*/, @Nullable Long parent /*put if it is reply*/);

    Call<VoteResponse> makeVote(String voteId, @Nullable Vote.Value voteValue);

    Call<CommentResponse> deleteComment(long commentId);

    Call<CertificateResponse> getCertificates();

    Single<UnitResponse> getUnitsByLessonId(long lessonId);

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

    Observable<StoryTemplatesResponse> getStoryTemplates(int page);
}

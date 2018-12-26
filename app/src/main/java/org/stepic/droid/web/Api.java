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
import org.stepik.android.model.user.User;

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

    Single<CoursesMetaResponse> getPopularCourses(int page);

    Call<StepicProfileResponse> getUserProfile();

    Call<UsersResponse> getUsers(long[] userIds);

    Single<List<User>> getUsersRx(long[] userIds);

    Completable joinCourse(long courseId);

    Completable dropCourse(long courseId);

    @Nullable
    Call<Void> dropCourse(@NotNull Course course);

    Call<SectionsMetaResponse> getSections(long[] sectionsIds);

    Single<SectionsMetaResponse> getSectionsRx(long[] sectionsIds);

    /**
     * Max number of  units defined in AppConstants
     */
    Call<UnitMetaResponse> getUnits(List<Long> units);

    Single<UnitMetaResponse> getUnitsRx(long[] units);

    Single<UnitMetaResponse> getUnits(long courseId, long lessonId);

    Call<LessonStepicResponse> getLessons(long[] lessons);

    Single<LessonStepicResponse> getLessonsRx(long[] lessons);

    Single<LessonStepicResponse> getLessons(long lessonId);

    Call<StepResponse> getSteps(long[] steps);

    Single<StepResponse> getStepsReactive(long[] steps);

    Single<StepResponse> getStepsByLessonId(long lessonId);

    Call<ProgressesResponse> getProgresses(String[] progresses);

    Single<ProgressesResponse> getProgressesReactive(String[] progresses);

    Call<AssignmentResponse> getAssignments(long[] assignmentsIds);

    Call<Void> postViewed(ViewAssignment stepAssignment);

    Completable postViewedReactive(ViewAssignment stepAssignment);

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery);

    Single<QueriesResponse> getSearchQueries(String query);

    Call<CoursesMetaResponse> getCourses(int page, long[] ids);

    Single<CoursesMetaResponse> getCoursesReactive(int page, @NotNull long[] ids);

    Single<CoursesMetaResponse> getCoursesReactive(@NotNull long[] ids);

    Call<AttemptResponse> createNewAttempt(long stepId);

    Single<AttemptResponse> createNewAttemptReactive(long stepId);

    Call<SubmissionResponse> createNewSubmission(Reply reply, long attemptId);

    Completable createNewSubmissionReactive(Submission submission);

    Call<AttemptResponse> getExistingAttempts(long stepId);

    Single<AttemptResponse> getExistingAttemptsReactive(long stepId);

    Call<SubmissionResponse> getSubmissions(long attemptId);

    Single<SubmissionResponse> getSubmissionsReactive(long attemptId);

    Call<SubmissionResponse> getSubmissionForStep(long stepId);

    Call<Void> remindPassword(String email);

    Call<EmailAddressResponse> getEmailAddresses(long[] ids);

    Call<Void> sendFeedback(String email, String rawDescription);

    Call<DeviceResponse> getDevices();

    Call<DeviceResponse> getDevicesByRegistrationId(String token);

    Call<DeviceResponse> renewDeviceRegistration(long deviceId, String token);

    Call<DeviceResponse> registerDevice(String token);

    Call<CoursesMetaResponse> getCourse(long id);

    Call<Void> setReadStatusForNotification(long notificationId, boolean isRead);

    Completable setReadStatusForNotificationReactive(long notificationId, boolean isRead);

    Call<Void> removeDevice(long deviceId);

    Call<DiscussionProxyResponse> getDiscussionProxies(String discussionProxyId);

    Call<CommentsResponse> getCommentAnd20Replies(long commentId);

    Call<CommentsResponse> getCommentsByIds(long[] commentIds);

    Call<CommentsResponse> postComment(String text, long target /*for example, related step*/, @Nullable Long parent /*put if it is reply*/);

    Call<VoteResponse> makeVote(String voteId, @Nullable Vote.Value voteValue);

    Call<CommentsResponse> deleteComment(long commentId);

    Call<CertificateResponse> getCertificates();

    Call<UnitMetaResponse> getUnitByLessonId(long lessonId);

    Call<NotificationResponse> getNotifications(NotificationCategory notificationCategory, int page);

    Call<Void> markAsReadAllType(@NotNull NotificationCategory notificationCategory);

    Single<NotificationStatusesResponse> getNotificationStatuses();

    Call<UserActivityResponse> getUserActivities(long userId);

    Single<UserActivityResponse> getUserActivitiesReactive(long userId);

    Call<LastStepResponse> getLastStepResponse(@NotNull String lastStepId);

    Single<CourseCollectionsResponse> getCourseCollections(String language);

    Single<CourseReviewResponse> getCourseReviews(long[] reviewSummaryIds);

    Single<TagResponse> getFeaturedTags();

    Single<SearchResultResponse> getSearchResultsOfTag(int page, @NotNull Tag tag);


    Single<RecommendationsResponse> getNextRecommendations(long courseId, int count);

    Completable createReaction(RecommendationReaction reaction);

    Single<List<RatingItem>> getRating(long courseId, int count, int days);

    Completable putRating(long courseId, long exp);

    Single<RatingRestoreResponse> restoreRating(long courseId);

    Observable<StoryTemplatesResponse> getStoryTemplates(int page);
}

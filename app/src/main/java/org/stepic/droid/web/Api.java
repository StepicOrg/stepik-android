package org.stepic.droid.web;

import android.support.v4.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.comments.VoteValue;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.social.SocialManager;

import java.io.IOException;

import io.reactivex.Single;
import retrofit2.Call;


public interface Api {

    enum TokenType {
        social, loginPassword
    }

    Call<AuthenticationStepicResponse> authWithNativeCode(String code, SocialManager.SocialType type, @Nullable String email);

    Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password);

    Call<AuthenticationStepicResponse> authWithCode(String code);

    Call<RegistrationResponse> signUp(String firstName, String secondName, String email, String password);

    Single<CoursesMetaResponse> getEnrolledCourses(int page);

    Single<CoursesMetaResponse> getPopularCourses(int page);

    Call<StepicProfileResponse> getUserProfile();

    Call<UsersResponse> getUsers(long[] userIds);

    Call<Void> tryJoinCourse(@NotNull Course course);

    Call<SectionsMetaResponse> getSections(long[] sectionsIds);

    /**
     * Max number of  units defined in AppConstants
     */
    Call<UnitMetaResponse> getUnits(long[] units);

    Call<LessonStepicResponse> getLessons(long[] lessons);

    Call<StepResponse> getSteps(long[] steps);

    Single<StepResponse> getStepsReactive(long[] steps);

    @Nullable
    Call<Void> dropCourse(long courseId);

    Call<ProgressesResponse> getProgresses(String[] progresses);

    Single<ProgressesResponse> getProgressesReactive(String[] progresses);

    Call<AssignmentResponse> getAssignments(long[] assignmentsIds);

    Call<Void> postViewed(ViewAssignment stepAssignment);

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery);

    Call<CoursesMetaResponse> getCourses(int page, long[] ids);

    Single<CoursesMetaResponse> getCoursesReactive(int page, @NotNull long[] ids);

    Call<AttemptResponse> createNewAttempt(long stepId);

    Call<SubmissionResponse> createNewSubmission(Reply reply, long attemptId);

    Call<AttemptResponse> getExistingAttempts(long stepId);

    Call<SubmissionResponse> getSubmissions(long attemptId);

    Call<SubmissionResponse> getSubmissionForStep(long stepId);

    Call<Void> remindPassword(String email);

    Call<EmailAddressResponse> getEmailAddresses(long[] ids);

    Call<Void> sendFeedback(String email, String rawDescription);

    Call<DeviceResponse> getDevices();

    Call<DeviceResponse> registerDevice(String token);

    Call<CoursesMetaResponse> getCourse(long id);

    Call<Void> setReadStatusForNotification(long notificationId, boolean isRead);

    Call<Void> removeDevice(long deviceId);

    Call<DiscussionProxyResponse> getDiscussionProxies(String discussionProxyId);

    UpdateResponse getInfoForUpdating() throws IOException;

    Call<CommentsResponse> getCommentAnd20Replies(long commentId);

    Call<CommentsResponse> getCommentsByIds(long[] commentIds);

    Call<CommentsResponse> postComment(String text, long target /*for example, related step*/, @Nullable Long parent /*put if it is reply*/);

    Call<VoteResponse> makeVote(String voteId, @Nullable VoteValue voteValue);

    Call<CommentsResponse> deleteComment(long commentId);

    Call<CertificateResponse> getCertificates();

    Call<UnitMetaResponse> getUnitByLessonId(long lessonId);

    Call<NotificationResponse> getNotifications(NotificationCategory notificationCategory, int page);

    Call<Void> markAsReadAllType(@NotNull NotificationCategory notificationCategory);

    Call<UserActivityResponse> getUserActivities(long userId);

    Call<LastStepResponse> getLastStepResponse(@NotNull String lastStepId);

    Single<CourseCollectionsResponse> getCourseCollections(String language);

    Single<CourseReviewResponse> getCourseReviews(int[] reviewSummaryIds);
}

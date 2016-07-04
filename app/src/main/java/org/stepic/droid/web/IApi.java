package org.stepic.droid.web;

import android.content.Context;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.comments.VoteValue;
import org.stepic.droid.social.ISocialType;

import java.io.IOException;

import retrofit.Call;


public interface IApi {
    enum TokenType {
        social, loginPassword
    }

    Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password);

    Call<AuthenticationStepicResponse> authWithCode(String code);

    Call<RegistrationResponse> signUp(String firstName, String secondName, String email, String password);

    Call<CoursesStepicResponse> getEnrolledCourses(int page);

    Call<CoursesStepicResponse> getFeaturedCourses(int page);

    Call<StepicProfileResponse> getUserProfile();

    Call<UserStepicResponse> getUsers(long[] userIds);

    Call<Void> tryJoinCourse(Course course);

    Call<SectionsStepicResponse> getSections(long[] sectionsIds);

    Call<UnitStepicResponse> getUnits(long[] units);

    Call<LessonStepicResponse> getLessons(long[] lessons);

    Call<StepResponse> getSteps(long[] steps);

    @Nullable
    Call<Void> dropCourse(long courseId);

    Call<ProgressesResponse> getProgresses(String[] progresses);

    Call<AssignmentResponse> getAssignments(long[] assignmentsIds);

    Call<Void> postViewed(ViewAssignment stepAssignment);

    void loginWithSocial(Context context, ISocialType type);

    Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery);

    Call<CoursesStepicResponse> getCourses(int page, long[] ids);

    Call<AttemptResponse> createNewAttempt(long stepId);

    Call<SubmissionResponse> createNewSubmission(Reply reply, long attemptId);

    Call<AttemptResponse> getExistingAttempts(long stepId);

    Call<SubmissionResponse> getSubmissions(long attemptId);

    Call<Void> remindPassword(String email);

    Call<EmailAddressResponse> getEmailAddresses(long[] ids);

    Call<Void> sendFeedback(String email, String rawDescription);

    Call<DeviceResponse> getDevices ();

    Call<DeviceResponse> registerDevice(String token);

    Call<CoursesStepicResponse> getCourse(long id);

    Call<Void> markNotificationAsRead (long notificationId, boolean isRead);

    Call<Void> removeDevice(long deviceId);

    Call<DiscussionProxyResponse> getDiscussionProxies (String discussionProxyId);

    UpdateResponse getInfoForUpdating() throws IOException;

    Call<CommentsResponse> getCommentAnd20Replies (long commentId);

    Call<CommentsResponse> getCommentsByIds (long [] commentIds);

    Call<CommentsResponse> postComment(String text, long target /*for example, related step*/, @Nullable Long parent /*put if it is reply*/);

    Call<VoteResponse> makeVote(String voteId, @Nullable VoteValue voteValue);

    Call<CommentsResponse> deleteComment(long commentId);
}

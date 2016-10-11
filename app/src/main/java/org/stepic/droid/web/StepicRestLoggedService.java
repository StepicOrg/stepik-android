package org.stepic.droid.web;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.EnrollmentWrapper;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface StepicRestLoggedService {
    @GET("api/sections")
    Call<SectionsStepicResponse> getSections(@Query("ids[]") long[] sectionIds);

    @Headers({"Content-Type : application/json"})
    @POST("api/enrollments")
    Call<Void> joinCourse(@Body EnrollmentWrapper enrollmentCourse);

    @GET("api/users")
    Call<UserStepicResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepicProfileResponse> getUserProfile();

    //todo:enrolled always true
    @GET("api/courses")
    Call<CoursesStepicResponse> getEnrolledCourses(@Query("enrolled") boolean enrolled,
                                                   @Query("page") int page);

    //todo:is_featured always true
    @GET("api/courses")
    Call<CoursesStepicResponse> getFeaturedCourses(@Query("is_featured_or_enrolled") boolean is_featured,
                                                   @Query("page") int page);

    @GET("api/units")
    Call<UnitStepicResponse> getUnits(@Query("ids[]") long[] units);

    @GET("api/lessons")
    Call<LessonStepicResponse> getLessons(@Query("ids[]") long[] lessons);

    @GET("api/steps")
    Call<StepResponse> getSteps(@Query("ids[]") long[] steps);

    @DELETE("api/enrollments/{id}")
    Call<Void> dropCourse(@Path("id") long courseId);

    @GET("api/progresses")
    Call<ProgressesResponse> getProgresses(@Query("ids[]") String[] progresses);

    @GET("api/assignments")
    Call<AssignmentResponse> getAssignments(@Query("ids[]") long[] assignmentsIds);


    @Headers({"Content-Type : application/json"})
    @POST("api/views")
    Call<Void> postViewed(@Body ViewAssignmentWrapper stepAssignment);

    @GET("api/search-results")
    Call<SearchResultResponse> getSearchResults(@Query("page") int page,
                                                @Query(value = "query", encoded = true) String encodedQuery, @Query("type") String type);

    @GET("api/courses")
    Call<CoursesStepicResponse> getCourses(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @POST("api/attempts")
    Call<AttemptResponse> createNewAttempt(@Body AttemptRequest attemptRequest);

    @POST("api/submissions")
    Call<SubmissionResponse> createNewSubmission(@Body SubmissionRequest submissionRequest);

    @GET("api/attempts")
    Call<AttemptResponse> getExistingAttempts(@Query("step") long stepId, @Query("user") long userId);

    @GET("api/submissions")
    Call<SubmissionResponse> getExistingSubmissions(@Query("attempt") long attemptId, @Query("order") String desc);

    @GET("api/email-addresses")
    Call<EmailAddressResponse> getEmailAddresses(@Query("ids[]") long[] ids);

    @GET("api/devices")
    Call<DeviceResponse> getDevices(@Query("user") long userId);

    @POST("api/devices")
    Call<DeviceResponse> registerDevice(@Body DeviceRequest deviceRequest);

    @GET("api/courses")
    Call<CoursesStepicResponse> getCourses(@Query("ids[]") long[] courseIds);

    @PUT("api/notifications/{id}")
    Call<Void> putNotification(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @DELETE("api/devices/{id}")
    Call<Void> removeDevice(@Path("id") long deviceId);

    @GET("api/discussion-proxies/{id}")
    Call<DiscussionProxyResponse> getDiscussionProxy(@Path("id") String discussionProxyId);

    @GET("api/comments")
    Call<CommentsResponse> getComments(@Query("ids[]") long[] ids);

    @POST("api/comments")
    Call<CommentsResponse> postComment(@Body CommentRequest comment);

    @PUT("api/votes/{id}")
    Call<VoteResponse> postVote(@Path("id") String voteId, @Body VoteRequest voteRequest);

    @DELETE("api/comments/{id}")
    Call<CommentsResponse> deleteComment(@Path("id") long commentId);

    @GET("api/certificates")
    Call<CertificateResponse> getCertificates(@Query("user") long userId);

    @GET("api/units")
    Call<UnitStepicResponse> getUnitByLessonId(@Query("lesson") long lessonId);

    @GET("api/steps")
    Call<StepResponse> geStepsByLessonId(@Query("lesson") long lessonId);

    @GET("api/submissions")
    Call<SubmissionResponse> getExistingSubmissionsForStep(@Query("step") long stepId);

    @GET("api/notifications")
    Call<NotificationResponse> getNotifications(@Query("page") int page, @Nullable @Query("type") String type);
}

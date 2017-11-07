package org.stepic.droid.web;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.EnrollmentWrapper;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StepicRestLoggedService {
    @GET("api/sections")
    Call<SectionsStepicResponse> getSections(@Query("ids[]") long[] sectionIds);

    @Headers({"Content-Type:application/json"})
    @POST("api/enrollments")
    Call<Void> joinCourse(@Body EnrollmentWrapper enrollmentCourse);

    @GET("api/users")
    Call<UserStepicResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepicProfileResponse> getUserProfile();

    @GET("api/courses?enrolled=true")
    Single<CoursesStepicResponse> getEnrolledCourses(@Query("page") int page);

    @GET("api/courses?is_public=true&order=-activity")
    Single<CoursesStepicResponse> getPopularCourses(@Query("page") int page);

    @GET("api/units")
    Call<UnitStepicResponse> getUnits(@Query("ids[]") long[] units);

    @GET("api/lessons")
    Call<LessonStepicResponse> getLessons(@Query("ids[]") long[] lessons);

    @GET("api/steps")
    Call<StepResponse> getSteps(@Query("ids[]") long[] steps);

    @GET("api/steps")
    Single<StepResponse> getStepsReactive(@Query("ids[]") long[] steps);

    @DELETE("api/enrollments/{id}")
    Call<Void> dropCourse(@Path("id") long courseId);

    @GET("api/progresses")
    Call<ProgressesResponse> getProgresses(@Query("ids[]") String[] progresses);

    @GET("api/assignments")
    Call<AssignmentResponse> getAssignments(@Query("ids[]") long[] assignmentsIds);


    @Headers({"Content-Type:application/json"})
    @POST("api/views")
    Call<Void> postViewed(@Body ViewAssignmentWrapper stepAssignment);

    @GET("api/search-results")
    Call<SearchResultResponse> getSearchResults(@Query("page") int page,
                                                @Query(value = "query", encoded = true) String encodedQuery, @Query("type") String type);

    @GET("api/courses")
    Call<CoursesStepicResponse> getCourses(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CoursesStepicResponse> getCoursesReactive(@Query("page") int page, @Query("ids[]") long[] courseIds);

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

    @GET("api/submissions?order=desc")
    Call<SubmissionResponse> getExistingSubmissionsForStep(@Query("step") long stepId);

    @GET("api/notifications")
    Call<NotificationResponse> getNotifications(@Query("page") int page, @Nullable @Query("type") String type);

    @FormUrlEncoded
    @POST("api/notifications/mark-as-read")
    Call<Void> markAsRead(@Nullable @Field(value = "type", encoded = true) String notificationType);

    @GET("api/user-activities/{userId}")
    Call<UserActivityResponse> getUserActivities(@Path("userId") long userId);

    @GET("api/last-steps/{lastStepId}")
    Call<LastStepResponse> getLastStepResponse(@Path("lastStepId") String lastStepId);

    @GET("api/course-lists?platform=mobile")
    Single<CourseListsResponse> getCourseLists(@Query("language") String language);
}

package org.stepic.droid.web;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.web.model.adaptive.RecommendationReactionsRequest;
import org.stepic.droid.web.model.adaptive.RecommendationsResponse;
import org.stepik.android.remote.assignment.model.AssignmentResponse;
import org.stepik.android.remote.attempt.model.AttemptRequest;
import org.stepik.android.remote.attempt.model.AttemptResponse;
import org.stepik.android.remote.auth.model.StepikProfileResponse;
import org.stepik.android.remote.certificate.model.CertificateResponse;
import org.stepik.android.remote.comment.model.CommentRequest;
import org.stepik.android.remote.comment.model.CommentResponse;
import org.stepik.android.remote.course.model.CourseResponse;
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse;
import org.stepik.android.remote.course.model.EnrollmentRequest;
import org.stepik.android.remote.discussion_proxy.model.DiscussionProxyResponse;
import org.stepik.android.remote.discussion_thread.model.DiscussionThreadResponse;
import org.stepik.android.remote.email_address.model.EmailAddressResponse;
import org.stepik.android.remote.last_step.model.LastStepResponse;
import org.stepik.android.remote.progress.model.ProgressResponse;
import org.stepik.android.remote.step.model.StepResponse;
import org.stepik.android.remote.unit.model.UnitResponse;
import org.stepik.android.remote.user.model.UserResponse;
import org.stepik.android.remote.user_activity.model.UserActivityResponse;
import org.stepik.android.remote.view_assignment.model.ViewAssignmentRequest;
import org.stepik.android.remote.vote.model.VoteRequest;
import org.stepik.android.remote.vote.model.VoteResponse;

import java.util.List;

import io.reactivex.Completable;
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
    @POST("api/enrollments")
    Completable joinCourse(@Body EnrollmentRequest enrollmentCourse);

    @DELETE("api/enrollments/{id}")
    Completable dropCourse(@Path("id") long courseId);

    @GET("api/users")
    Call<UserResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/users")
    Single<UserResponse> getUsersRx(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepikProfileResponse> getUserProfile();

    @GET("api/user-courses")
    Single<UserCoursesResponse> getUserCourses(@Query("page") int page);

    @GET("api/courses?exclude_ended=true&is_public=true&order=-activity")
    Single<CourseResponse> getPopularCourses(@Query("page") int page, @Query("language") String language);

    @GET("api/units")
    Call<UnitResponse> getUnits(
            @Query("ids[]") List<Long> units
    );

    @GET("api/units")
    Single<UnitResponse> getUnits(
            @Query("course") final long courseId,
            @Query("lesson") final long lessonId
    );

    @GET("api/steps")
    Single<StepResponse> getSteps(
            @Query("ids[]") long[] steps
    );

    @GET("api/steps")
    Single<StepResponse> getStepsByLessonId(
            @Query("lesson") long lessonId
    );

    @GET("api/progresses")
    Call<ProgressResponse> getProgresses(@Query("ids[]") String[] progresses);

    @GET("api/progresses")
    Single<ProgressResponse> getProgressesReactive(@Query("ids[]") String[] progresses);

    @GET("api/assignments")
    Single<AssignmentResponse> getAssignments(@Query("ids[]") long[] assignmentsIds);


    @Headers("Content-Type:application/json")
    @POST("api/views")
    Completable postViewedReactive(@Body ViewAssignmentRequest stepAssignment);

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Call<SearchResultResponse> getSearchResults(
            @Query("page") int page,
            @Query(value = "query", encoded = true) String encodedQuery,
            @Query("language") String lang
    );

    @GET("api/queries")
    Single<QueriesResponse> getSearchQueries(@Query("query") String query);

    @GET("api/courses")
    Call<CourseResponse> getCourses(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CourseResponse> getCoursesReactive(@Query("page") int page, @Query("ids[]") long[] courseIds);

    @GET("api/courses")
    Single<CourseResponse> getCoursesReactive(@Query("ids[]") long[] courseIds);

    @POST("api/attempts")
    Single<AttemptResponse> createNewAttemptReactive(@Body AttemptRequest attemptRequest);

    @GET("api/attempts")
    Single<AttemptResponse> getExistingAttemptsReactive(@Query("step") long stepId, @Query("user") long userId);

    @GET("api/attempts")
    Single<AttemptResponse> getExistingAttemptsReactive(@Query("ids[]") long[] ids);

    @GET("api/email-addresses")
    Call<EmailAddressResponse> getEmailAddresses(@Query("ids[]") long[] ids);

    @GET("api/devices")
    Call<DeviceResponse> getDeviceByRegistrationId(@Query("registration_id") String token);

    @POST("api/devices")
    Call<DeviceResponse> registerDevice(@Body DeviceRequest deviceRequest);

    @PUT("api/devices/{id}")
    Call<DeviceResponse> renewDeviceRegistration(@Path("id") long deviceId, @Body DeviceRequest deviceRequest);

    @GET("api/courses")
    Call<CourseResponse> getCourses(@Query("ids[]") long[] courseIds);

    @PUT("api/notifications/{id}")
    Call<Void> putNotification(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @PUT("api/notifications/{id}")
    Completable putNotificationReactive(@Path("id") long notificationId, @Body NotificationRequest notificationRequest);

    @GET("api/discussion-proxies")
    Single<DiscussionProxyResponse> getDiscussionProxies(@Query("ids[]") String[] ids);

    @GET("api/discussion-threads")
    Single<DiscussionThreadResponse> getDiscussionThreads(@Query("ids[]") String[] ids);

    @GET("api/comments")
    Single<CommentResponse> getComments(@Query("ids[]") long[] ids);

    @POST("api/comments")
    Single<CommentResponse> createComment(@Body CommentRequest request);

    @PUT("api/comments/{commentId}")
    Single<CommentResponse> saveComment(@Path("commentId") long commentId, @Body CommentRequest request);

    @DELETE("api/comments/{commentId}")
    Completable removeComment(@Path("commentId") long commentId);

    @PUT("api/votes/{id}")
    Single<VoteResponse> saveVote(@Path("id") String voteId, @Body VoteRequest voteRequest);

    @GET("api/certificates")
    Single<CertificateResponse> getCertificates(@Query("user") long userId, @Query("page") int page);

    @GET("api/notifications")
    Call<NotificationResponse> getNotifications(@Query("page") int page, @Nullable @Query("type") String type);

    @FormUrlEncoded
    @POST("api/notifications/mark-as-read")
    Call<Void> markAsRead(@Nullable @Field(value = "type", encoded = true) String notificationType);

    @GET("api/notification-statuses")
    Single<NotificationStatusesResponse> getNotificationStatuses();

    @GET("api/user-activities/{userId}")
    Call<UserActivityResponse> getUserActivities(@Path("userId") long userId);

    @GET("api/user-activities/{userId}")
    Single<UserActivityResponse> getUserActivitiesReactive(@Path("userId") long userId);

    @GET("api/last-steps/{lastStepId}")
    Single<LastStepResponse> getLastStepResponse(@Path("lastStepId") String lastStepId);

    @GET("api/course-lists?platform=mobile")
    Single<CourseCollectionsResponse> getCourseLists(@Query("language") String language);

    @GET("api/course-review-summaries")
    Single<CourseReviewSummaryResponse> getCourseReviews(@Query("ids[]") long[] reviewSummaryIds);

    @GET("api/tags?is_featured=true")
    Single<TagResponse> getFeaturedTags();

    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    Single<SearchResultResponse> getSearchResultsOfTag(@Query("page") int page, @Query("tag") int id, @Query("language") String lang);

    @GET("api/recommendations")
    Single<RecommendationsResponse> getNextRecommendations(
            @Query("course") final long courseId,
            @Query("count") final int count
    );

    @POST("api/recommendation-reactions")
    Completable createRecommendationReaction(
            @Body final RecommendationReactionsRequest reactionsRequest
    );
}
